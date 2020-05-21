package com.leyou.order.controller;

import com.egzosn.pay.ali.api.AliPayConfigStorage;
import com.egzosn.pay.ali.api.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping("notify")
public class NotifyController {
    @Autowired
    private AliPayConfigStorage storage;

    @PostMapping
    public void test(HttpServletRequest request) throws IOException {
        // todo 支付宝回调处理
        System.out.println("开始运行.......");
        AliPayService service = new AliPayService(storage);
        System.out.println(service.payBack(request.getParameterMap(), request.getInputStream()).toMessage());
    }


}
