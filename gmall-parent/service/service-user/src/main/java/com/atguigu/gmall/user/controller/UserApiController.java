package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-27 9:38
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    @Autowired
    LoginService loginService;
    @GetMapping("inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") String userId){
        return loginService.findUserAddressListByUserId(userId);
    }
}
