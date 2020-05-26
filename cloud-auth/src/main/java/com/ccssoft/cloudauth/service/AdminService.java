package com.ccssoft.cloudauth.service;

import com.ccssoft.cloudauth.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author moriarty
 * @date 2020/5/26 15:49
 */
@Component
@FeignClient(value = "admin-server")//调用哪个微服务
public interface AdminService {

    @PostMapping(value = "auth/register")
    public String registerUser(@RequestBody User user);

    @GetMapping("/auth/login")
    public String login();
}
