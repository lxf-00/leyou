package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {
    /**
     * 用户登录验证
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    User queryUserByUsernamePassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );
}
