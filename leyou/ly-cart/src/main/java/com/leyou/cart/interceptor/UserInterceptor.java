package com.leyou.cart.interceptor;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    private JwtProperties prop;

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    // 线程域
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取cookie中token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        // 解析cookie
        try {
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 传递user
            tl.set(info);
            // 放行
            return true;
        } catch (Exception e){
            // 记录日志
            log.error("【购物车微服务】解析用户身份失败！", e);
            // 解析失败，未登录: 拦截
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 最后使用完数据需要清空
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
