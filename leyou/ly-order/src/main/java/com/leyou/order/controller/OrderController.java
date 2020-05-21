package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     * 根据商品订单id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 支付宝支付页面
     * @param id
     * @return
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> toPay(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.toPay(id));
    }
}
