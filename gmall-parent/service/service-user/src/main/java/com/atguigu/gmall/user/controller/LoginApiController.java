package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-23 14:37
 */
@RestController
@RequestMapping("/api/user/passport")
public class LoginApiController {

    @Autowired
    private LoginService loginService;

    @Autowired
    RedisTemplate redisTemplate;

    @PostMapping("/login")
    public Result login(@RequestBody UserInfo userInfo){
        //1.判断用户名不能为空  太简单了不写了就
        //2.判断密码为不为空
        //3.判断用户名密码是否正确   返回值需要有我们的用户名   最好显示我们的昵称，为了安全起见
        UserInfo user = loginService.login(userInfo);
        if (user==null){
            return Result.fail().message("此用户名或者密码不正确");
        }else {
            //生成令牌
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            //保存令牌
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token,user.getId(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            Map map = new HashMap<>();
            map.put("token",token);
            map.put("nickName",user.getNickName());
            return Result.ok(map);
        }
    }

    @GetMapping("/logout")
    public Result logout(HttpServletRequest httpServletRequest){
        String token = httpServletRequest.getHeader("token");
        System.out.println(token);
        if (!StringUtils.isEmpty(token)){
            redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+token);
        }
        return Result.ok();
    }
}
