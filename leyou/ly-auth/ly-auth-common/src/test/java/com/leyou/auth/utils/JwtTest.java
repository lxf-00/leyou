package com.leyou.auth.utils;

import com.leyou.auth.entity.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "/Users/lxf/Desktop/rsa.pub";
    private static final String priKeyPath = "/Users/lxf/Desktop/rsa.pri";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4OTMzNTIyMH0.dLTRV1dLaa_2O4gceJW_1pdSfMyAo2lWGFBotOKJsxzHjoIVEvAfjk4QHooWE8kWtesBlTrYBi6Te5mw-8jzt3EDRmu58h35-z7N2nNIjwZNMBznfnRrOEq4Ndv2sSm557VlhKcC5CJwpbJPpJUL1vy0_ON3MmiOzsSEphnQPUk";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
