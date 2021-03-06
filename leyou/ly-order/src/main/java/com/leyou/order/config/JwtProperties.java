package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;// 公钥地址
    private PublicKey publicKey;  // 公钥
    private String cookieName;

    // 对象实例化后，读取公钥和私钥进内存
    @PostConstruct
    public void init() throws Exception {
        // 读取公钥和私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}
