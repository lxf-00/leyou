package com.leyou.sms.mq;


import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.Arrays;
import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue", durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC),
            key = {"sms.verify.code"}
    ))
    public void listenSendSms(Map<String, Object> map){
        // 判断集合是否为空
        if(CollectionUtils.isEmpty(map)){
            // 直接返回
            return;
        }
        // 获取接收人信息
        String receiver = (String) map.get("receiver");
        if(StringUtils.isBlank(receiver)){
            return;
        }
        // 获取发送的数据信息
        String datas = (String) map.get("datas");
        String[] split = datas.split(",");
        if(CollectionUtils.isEmpty(Arrays.asList(split))){
            return;
        }
        // 发送验证码
        smsUtils.sendSms(receiver, split);

    }
}
