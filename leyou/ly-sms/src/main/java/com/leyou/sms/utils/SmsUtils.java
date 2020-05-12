package com.leyou.sms.utils;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@EnableConfigurationProperties(SmsProperties.class)
@Slf4j
public class SmsUtils {

    @Autowired
    private SmsProperties prop;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "sms:phone:";
    private static final long SMS_MIN_INTERVAL_IN_MILLIS = 60000;

    /**
     * 发送短信工具类
     * @param receiver 接收方电话（字符串，多个以 ","分隔）
     * @param datas 内容数组 {"验证码","时效"}
     */
    public void sendSms(String receiver, String[] datas) {
        // 按照手机号码限流
        String rKey = KEY_PREFIX + receiver;
        // 读取时间
        String lastTime = redisTemplate.opsForValue().get(rKey);
        if(StringUtils.isNotBlank(lastTime)){
            Long last = Long.valueOf(lastTime);
            if(System.currentTimeMillis() - last < SMS_MIN_INTERVAL_IN_MILLIS){
                log.info("【短信微服务】发送短信频率过高，被阻止!, 手机号码：{}", receiver);
                return;
            }
        }
        try {
            CCPRestSmsSDK sdk = new CCPRestSmsSDK();
            // 初始化服务器地址和端口
            sdk.init(prop.getSeverIp(), prop.getServerPort());
            // 初始化账户
            sdk.setAccount(prop.getAccountSId(), prop.getAccountToken());
            // 初始化模板
            sdk.setAppId(prop.getAppId());
            // 设置编码格式
            // sdk.setBodyType(BodyType.Type_JSON);

            // 发送验证码
            HashMap<String, Object> result = sdk.sendTemplateSMS(receiver, prop.getTemplateId(), datas);
            // 判断是否成功发送
            if ("000000".equals(result.get("statusCode"))) {
                //正常返回输出data包体信息（map）
                HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
                Set<String> keySet = data.keySet();
                for (String key : keySet) {
                    Object object = data.get(key);
                    System.out.println(key + " = " + object);
                }
                // 成功后写入redis,指定有效时间
                redisTemplate.opsForValue().set(rKey, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);
            } else {
                //异常返回输出错误码和错误信息
                System.out.println("错误码=" + result.get("statusCode") + " 错误信息= " + result.get("statusMsg"));
            }
        } catch (Exception e){
            log.error("【短信微服务】发送短信验证码异常,手机号码:{}", receiver, e);
        }
    }
}
