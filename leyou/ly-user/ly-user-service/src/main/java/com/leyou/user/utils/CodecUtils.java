package com.leyou.user.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CodecUtils {

    public static String md5Hex(String data, String salt){
        if(StringUtils.isBlank(salt)){
            salt = data.hashCode() + "";
        }
        return DigestUtils.md5DigestAsHex((salt + DigestUtils.md5DigestAsHex(data.getBytes())).getBytes());
    }

    public static String generateSalt(){
        return StringUtils.replace(UUID.randomUUID().toString(),"-", "" );
    }
}
