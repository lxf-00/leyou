package com.leyou.order.service;

import com.egzosn.pay.ali.api.AliPayConfigStorage;
import com.egzosn.pay.ali.api.AliPayService;
import com.egzosn.pay.ali.bean.AliTransactionType;
import com.egzosn.pay.common.api.PayService;
import com.egzosn.pay.common.bean.MethodType;
import com.egzosn.pay.common.bean.PayOrder;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.util.AlipayProperties;
import com.leyou.pojo.Sku;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private AliPayConfigStorage configStorage;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        // 1, 新增订单
        Order order = new Order();
        // 1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());

        // 1.2 获取用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        // 1.3 收货人信息
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());

        // 1.4 金额
        // 把cartDTO转换为map,key 为sku id, value为num;
        Map<Long, Integer> numMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        // 获取所有的sku id
        Set<Long> skuIds =numMap.keySet();
        // 根据sku ids 查询sku
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(skuIds));
        // 准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();

        long totalPay = 0L;
        for (Sku  sku : skus) {
            // 计算商品的总金额
            totalPay +=  sku.getPrice() * numMap.get(sku.getId());

            // 封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());

            details.add(detail);

        }
        order.setTotalPay(totalPay);
        order.setActualPay(totalPay + order.getPostFee() - 0);

        // 1.5 order写入数据库
        int count = orderMapper.insertSelective(order);
        if(count != 1){
            log.error("【订单微服务】订单创建失败！orderId: {}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 2, 新增订单详情
        count = detailMapper.insertList(details);
        if(count != details.size()){
            log.error("【订单微服务】订单创建失败！orderId: {}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 3， 新增订单状态
        OrderStatus status = new OrderStatus();
        status.setCreateTime(order.getCreateTime());
        status.setOrderId(orderId);
        status.setStatus(OrderStatusEnum.UNPAY.value());

        count = statusMapper.insertSelective(status);
        if(count != 1){
            log.error("【订单微服务】订单创建失败！orderId: {}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 4，减库存
        List<CartDTO> carts = orderDTO.getCarts();
        goodsClient.decreaseStock(carts);

        return orderId;
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    public Order queryOrderById(Long id) {
        // 查询订单信息
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 查询订单详情：orderDetail
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if(CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);

        // 查询订单状态
        OrderStatus status = statusMapper.selectByPrimaryKey(id);
        if (status == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(status);
        return order;
    }

    /**
     * 跳转到支付宝支付页面
     * @param id
     * @return
     */
    public String toPay(Long id) {
        try {
            // 1, 获取相关订单信息
            Order order = this.queryOrderById(id);
            // 1.2 判断订单状态
            Integer status = order.getOrderStatus().getStatus();
            if(status != OrderStatusEnum.UNPAY.value()){
                throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
            }
            // 2, 创建支付服务
            AliPayService payService = new AliPayService(configStorage);
            List<OrderDetail> details = order.getOrderDetails();
            // 3, 封装订单信息
            String body = details.stream().map(OrderDetail::getTitle).collect(Collectors.joining("/"));
            PayOrder payOrder = new PayOrder("乐优商城购物", body, new BigDecimal(order.getActualPay() / 100), String.valueOf(id));
            // 4,电脑网页支付
            payOrder.setTransactionType(AliTransactionType.PAGE);
            // 5, 获取支付所需的信息
            Map<String, Object> info = payService.orderInfo(payOrder);
            // 6，获取表单提交对应的字符串，将其序列化到页面即可
            String directHtml = payService.buildRequest(info, MethodType.POST);
            // 7, 返回应答
            return directHtml;
        } catch (Exception e){
            log.error("【订单微服务】创建支付接入异常，订单:{}",id, e);
            throw new LyException(ExceptionEnum.CREATE_PAYMENT_ERROR);
        }
    }
}
