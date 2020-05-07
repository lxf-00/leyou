package com.leyou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter(){
        // 1. 添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        // 2. 允许的域，不要写*，否则cookie无法使用
        config.addAllowedOrigin("http://manage.leyou.com");
        config.addAllowedOrigin("http://www.leyou.com");
        // 3. 是否发送cookie信息
        config.setAllowCredentials(true);
        // 4. 允许请求的参数
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        // 5. 允许的头信息
        config.addAllowedHeader("*");
        // 6. 有效时长
        config.setMaxAge(3600L);
        // 7. 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**",config );
        // 8. 返回新的CorsFilter
        return new CorsFilter(configSource);
    }
}
