package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-23 14:42
 */
public interface LoginService {
    UserInfo login(UserInfo userInfo);

    List<UserAddress> findUserAddressListByUserId(String userid);
}
