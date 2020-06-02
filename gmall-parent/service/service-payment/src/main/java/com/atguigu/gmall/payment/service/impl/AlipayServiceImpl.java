package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 13:46
 */
@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Override
    public String submit(Long orderId) {
        PaymentInfo paymentInfo = paymentInfoService.savaPaymentInfo(orderId, PaymentType.ALIPAY);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        Map map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());
        request.setBizContent(JSONObject.toJSONString(map));
        //转发的页面
        request.setReturnUrl(AlipayConfig.return_payment_url);
        //异步给商家 的地址
        request.setNotifyUrl(AlipayConfig.notify_payment_url);

        AlipayTradePagePayResponse response =null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()){
            System.out.println("支付成功");
        }else {
            System.out.println("支付失败");
        }
        return response.getBody();
    }

    @Override
    public void refund(String outTradeNo) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map<Object, Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("refund_amount","23996.00");
        request.setBizContent(JSONObject.toJSONString(map));
        AlipayTradeRefundResponse response =null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()){
            System.out.println("调用退钱成功");
        }else {
            System.out.println("支付失败");
        }
    }
}
