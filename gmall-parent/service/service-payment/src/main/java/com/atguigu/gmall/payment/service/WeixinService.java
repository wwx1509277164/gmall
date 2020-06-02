package com.atguigu.gmall.payment.service;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-02 8:05
 */
public interface WeixinService {
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId, String name);
}
