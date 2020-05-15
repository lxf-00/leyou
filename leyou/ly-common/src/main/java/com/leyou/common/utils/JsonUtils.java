package com.leyou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;


import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author: HuYi.Zhang
 * @create: 2018-04-24 17:20
 **/
public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    @Nullable
    public static String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }

    @Nullable
    public static <T> T parse(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <E> List<E> parseList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class User {
        String name;
        Integer age;
    }

    public static void main(String[] args) {
        User user = new User("John", 23);

        // 测试serialize方法： 序列化为json格式
        /*String jsonStr = serialize(user);
        System.out.println("jsonStr = " + jsonStr);*/

        // 反序列化(字符串）：parse
        // User user1 = parse(jsonStr, User.class);
        // System.out.println("user1 = " + user1);

        // 反序列化（列表）： parseList
        /*String jsonStr = "[1,2,3,4,5]";
        List<Integer> list = parseList(jsonStr, Integer.class);
        System.out.println("list = " + list);*/


        // 反序列化（集合）：parseMap
        //language=JSON
        /*String json = "{\"name\" : \"jack\", \"age\" : \"23\"}\n";
        Map<String, String> map = parseMap(json, String.class, String.class);
        System.out.println("map = " + map);*/
        
        // 反序列化（复杂类型）： nativeRed
//        String json = "[{\"name\" : \"jack\", \"age\" : \"23\"}\n]";
//        List<Map<String, String>> maps = nativeRead(json, new TypeReference<List<Map<String, String>>>() {
//        });
//        for (Map<String, String> map : maps) {
//            System.out.println("map = " + map);
//        }

        String json = "[{\"group\":\"主体\", \"params\":[{\"k\":\"品牌\", \"v\":\"华为\"},{\"k\":\"型号\",\"v\":\"v3.0\" }]},{\"group\":\"规格\", \"params\":[{\"k\":\"重量\", \"v\":\"132\"},{\"k\":\"型号\",\"v\":\"v3.0\" }]}]";
        List<Map<String, Object>> maps = nativeRead(json, new TypeReference<List<Map<String, Object>>>() {
        });
        for (Map<String, Object> map : maps) {
            List<Map<String,String>> o = (List<Map<String,String>>)map.values().toArray()[1];
//            System.out.println("o = " + o);
            List<String> key = o.stream().map(o1 -> {
                String k = o1.get("k");
                return k;
            }).collect(Collectors.toList());
            System.out.println("key = " + key);
        }
    }


}

