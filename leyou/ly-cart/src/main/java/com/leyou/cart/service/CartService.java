package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid:";

    /**
     * 新增购物车商品
     * @param cart
     */
    @Transactional
    public void addCart(Cart cart) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // 获取用户购物车数据
        String key = KEY_PREFIX + user.getId();
        // hashKey
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        // 记录num
        Integer num = cart.getNum();

        // 判断是否存在
        if (operation.hasKey(hashKey)) {
            // 是，增加数量
            String json = operation.get(hashKey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(num + cart.getNum());
        }
        // 写回redis
        operation.put(hashKey,JsonUtils.serialize(cart));


    }

    /**
     * 查询用户购物车信息
     * @return
     */
    public List<Cart> queryCartList() {
        // 获取登录用户信息
        UserInfo user = UserInterceptor.getUser();
        // 获取相关信息：redis
        String key = KEY_PREFIX + user.getId();
        if(!redisTemplate.hasKey(key)){
            // 不存在
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Cart> carts = operations.values().stream().map(o -> {
            String s = o.toString();
            return JsonUtils.parse(s, Cart.class);
        }).collect(Collectors.toList());

        return  carts;
    }

    /**
     * 更新购物车商品数量
     * @param skuId
     * @param num
     */
    @Transactional
    public void updateCart(Long skuId, Integer num) {
       //  key
        String key = KEY_PREFIX + UserInterceptor.getUser().getId();
        // 判断是否存在该商品
        String hashKey = String.valueOf(skuId);
        // 绑定key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if (!operations.hasKey(hashKey)) {
            // 不存在,抛出异常
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        // 获取用户的购物车内相关商品
        Cart cart = JsonUtils.parse(operations.get(hashKey).toString(), Cart.class);
        // 更新商品数量
        cart.setNum(num);
        // 写回redis
        operations.put(hashKey, JsonUtils.serialize(cart));
    }

    /**
     * 删除购物车商品
     * @param id
     */
    @Transactional
    public void deleteCart(Long id) {
        //  key
        String key = KEY_PREFIX + UserInterceptor.getUser().getId();
        // 判断是否存在该商品
        String hashKey = String.valueOf(id);
        // 绑定key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        // 删除
        operations.delete(hashKey);
    }
}
