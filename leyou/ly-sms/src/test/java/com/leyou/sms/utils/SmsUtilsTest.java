package com.leyou.sms.utils;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsUtilsTest {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private AmqpTemplate template;

    @Test
    public void testSmsUtils(){
        Map<String, Object> msg = new HashMap<>();
        msg.put("receiver", "18575678578");
        msg.put("datas",  "9090,1");
        template.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);



    }

}
