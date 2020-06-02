package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 13:54
 */
public interface PaymentInfoService {
    PaymentInfo savaPaymentInfo(Long orderId, PaymentType type);

    void paySuccess(Map<String, String> paramsMap);
}
