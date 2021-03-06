package com.atguigu.gmall.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.LoginService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-23 14:42
 */
@Component
public class LoginServiceImpl implements LoginService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Override
    public UserInfo login(UserInfo userInfo) {

        //1.获取登录名
        //2.获取密码
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", userInfo.getLoginName());
        queryWrapper.eq("passwd", password);
        UserInfo info = userInfoMapper.selectOne(queryWrapper);
        if (info != null) {
            return info;
        }
        return null;
    }

    //查询地址集合

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        return userAddressMapper.selectList(new QueryWrapper<UserAddress>().eq("user_id",userId));
    }
}
