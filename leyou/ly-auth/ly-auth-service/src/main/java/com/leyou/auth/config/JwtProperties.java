package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String secret; // 密钥
    private String pubKeyPath;// 公钥地址
    private String priKeyPath;// 私钥地址
    private int expire;// token过期时间

    private PublicKey publicKey;  // 公钥
    private PrivateKey privateKey; // 私钥
    private String cookieName;

    // 对象实例化后，读取公钥和私钥进内存
    @PostConstruct
    public void init() throws Exception {
        // 公钥私钥不存在，先生成
        File pubPath = new File(pubKeyPath);
        File priPath = new File(priKeyPath);
        if(!pubPath.exists() || !priPath.exists()){
            // 生成公钥私钥
            RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
        }
        // 读取公钥和私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

}
