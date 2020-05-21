package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("ly.worker")
public class IdWorkerProperties {
    private Long workerId;
    private Long dataCenterId;
}
