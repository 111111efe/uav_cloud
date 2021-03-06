package com.ccssoft.cloudadmin.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccssoft.cloudadmin.dao.UserDao;
import com.ccssoft.cloudadmin.filter.RedisBloomFilter;
import com.ccssoft.cloudadmin.util.RedisUtil;
import com.ccssoft.cloudcommon.entity.User;
import com.ccssoft.cloudadmin.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author moriarty
 * @date 2020/5/20 10:04
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {
    @Resource
    private UserDao userDao;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisBloomFilter bloomFilter;

    @Override
    public User getUserByUsername(String username) {
        if (!bloomFilter.isExist(username)) {
            return null;
        }

//        if (redisUtil.get(username) == null) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username",username);
            User user = userDao.selectOne(wrapper);
            redisUtil.set(username,JSONUtil.parse(user),600);
            return user;
//        }
//        JSONObject jsonObject = JSONUtil.parseObj(redisUtil.get(username));
//        return jsonObject.toBean(User.class);

    }

    @Override
    public String getSaltByUsername(String username) {
        return userDao.getSaltByUsername(username);
    }


    @Override
    public Page getUserByPage(int current, int size) {
        //TODO 先这么存，后续再优化.不能这样存，管理者一停用账户就不会刷新了
//        if (redisUtil.get(""+current+size) == null) {
            Page<User> page = new Page<>(current,size);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("role_id",2);
            userDao.selectPage(page,wrapper);
            redisUtil.set(""+current+size,page,6000);
            return page;
//        }
//        JSONObject jsonObject = JSONUtil.parseObj(redisUtil.get(""+current+size));
//        return jsonObject.toBean(Page.class);

    }

    @Override
    public boolean saveDB(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper();
        wrapper.eq("username",user.getUsername());
        if (userDao.selectOne(wrapper) == null) {
            int result = userDao.insert(user);
            //新增时需要处理一下布隆过滤器的列表
            bloomFilter.put(String.valueOf(user.getUsername()));
            return result ==1 ? true : false;
        }
        return false;
    }

    @Override
    public Map getVerificationCode() {
        //定义图形验证码的长和宽
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(100, 40);
        String img = captcha.getImageBase64();
        String code = captcha.getCode();

        Map<String,Object> map = new HashMap<>();
        map.put("captcha","data:image/png;base64,"+img);
        map.put("key",code);

        redisUtil.set(code,img,60);
        return map;
    }

    @Override
    public int getUserCount() {
        if (redisUtil.get("userCount") == null) {
            QueryWrapper<User> wrapper = new QueryWrapper();
            wrapper.eq("role_id",2);
            Integer integer = userDao.selectCount(wrapper);
            redisUtil.set("userCount",integer);
            return integer;
        }

        return (int)redisUtil.get("userCount");
    }
}
