package com.leyou.order.util;

import com.egzosn.pay.ali.api.AliPayConfigStorage;



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付助手
 * egzosn alipay
 * github: https://github.com/egzosn/pay-java-parent/tree/dev/pay-java-ali
 */
@Configuration
public class AlipayProperties {

    @Bean
    @ConfigurationProperties(prefix = "ly.pay")
    public AliPayConfigStorage getConfig(){
        return new AliPayConfigStorage();
    }
}
