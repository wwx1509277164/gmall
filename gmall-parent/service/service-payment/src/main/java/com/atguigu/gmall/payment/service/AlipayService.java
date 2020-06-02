package com.atguigu.gmall.payment.service;

/**
 * @author Administrator
 * @create 2020-06-01 13:46
 */
public interface AlipayService {
    String submit(Long orderId);

    void refund(String outTradeNo);
}
