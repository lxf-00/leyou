package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {
    private String severIp;
    private String serverPort;
    private String accountSId;
    private String accountToken;
    private String appId;
    private String templateId;
}
