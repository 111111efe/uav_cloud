package com.ccssoft.cloudadmin.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.Console;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ccssoft.cloudcommon.entity.User;
import com.ccssoft.cloudadmin.service.UserService;
import com.ccssoft.cloudcommon.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


/**
 * @author moriarty
 * @date 2020/5/20 09:50
 */
@CrossOrigin(origins = "*",maxAge = 3600)
@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    @Resource
    private UserService userService;

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    @ResponseBody
    public R registerUser(@Valid @RequestBody User user) {
        log.info("进入AuthController.registerUser(),参数={}",user);
        //处理一下密码加密，暂时先不用了，毕竟security已自带
        //String salt = String.valueOf((int)(Math.random()*1000000));
        //user.setSalt(salt);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userService.saveDB(user) ? R.ok() :R.error(300,"用户注册失败或用户名重复");
    }
    @PostMapping("/changePassword")
    @ResponseBody
    public R changePassword (@Valid @RequestBody User user) {
        log.info("进入AuthController.changePassword(),参数={}",user);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userService.updateById(user) ? R.ok() : R.error(300,"密码修改失败！");
    }

    @PostMapping("/updateInfo")
    @ResponseBody
    public R updateInfo (@Valid @RequestBody User user) {
        log.info("进入AuthController.updateInfo(),参数={}",user);
        return userService.updateById(user) ?R.ok() : R.error(300,"信息修改失败");
    }

    @GetMapping("/deleteUser/{id}")
    @ResponseBody
    public R delUser (@PathVariable("id") Long id) {
        log.info("进入AuthController.delUser(),参数:userId={}",id);
        return userService.removeById(id) ? R.ok() : R.error(301,"删除失败！");
    }

    @GetMapping("/getUser/{username}")
    @ResponseBody
    public R getInfo(@PathVariable("username") String username) {
        log.info("进入AuthController.getInfo(),参数={}",username);
        User user = userService.getUserByUsername(username);
        return user != null ? R.ok(user) : R.error(301,"不存在此用户！");
    }

    /**
     * 获取所有游客的用户分页信息
     * @param current 当前页数
     * @param size 每页数据量
     * @return 分页数据
     */
    @GetMapping("/getUser4Page/{current}&{size}")
    @ResponseBody
    public R getUser4Page(@PathVariable("current") int current, @PathVariable("size") int size){
        log.info("进入AuthController.getUserByPage(),参数={}",current+","+size);
        Page page = userService.getUserByPage(current,size);
        return R.ok(page);
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/verificationCode")
    @ResponseBody
    public R getVerificationCode (HttpServletResponse response) {
        //定义图形验证码的长和宽
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);
        String img = captcha.getImageBase64();
        String code = captcha.getCode();
        Console.log(code);

        Map<String,Object> map = new HashMap<>();
        map.put("captcha","data:image/png;base64,"+img);

        return R.ok(map);
    }


}
