package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.common.util.HttpClient;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.OrderInfoMapper;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.atguigu.gmall.payment.service.WeixinService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-06-02 8:06
 */
@Service
public class WeixinServiceImpl implements WeixinService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.partnerkey}")
    private String partnerkey;

    @Value("${weixin.notifyurl}")
    private String notifyurl;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Map createNative(Long orderId) {
        try{
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            PaymentInfo paymentInfo = paymentInfoService.savaPaymentInfo(orderId, PaymentType.WEIXIN);
            Map m = new HashMap<>();
            m.put("appid", appid);
            m.put("mch_id", partner);
            m.put("nonce_str", WXPayUtil.generateNonceStr());
            m.put("body", orderInfo.getTradeBody());
            m.put("out_trade_no", orderInfo.getOutTradeNo());
            m.put("total_fee", "100");
            m.put("spbill_create_ip", "127.0.0.1");
            m.put("notify_url", notifyurl);
            m.put("trade_type", "NATIVE");
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(m, partnerkey));
            client.setHttps(true);

            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getTotalAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));

            //微信支付二维码2小时过期，可采取2小时未支付取消订单
            redisTemplate.opsForValue().set(orderId.toString(), map, 120, TimeUnit.MINUTES);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }
    //微信支付成功回掉函数
    @Override
    public Map<String, String> queryPayStatus(Long orderId, String name) {
        try {
            PaymentInfo paymentInfoQuery = paymentInfoService.getPaymentInfo(orderId, PaymentType.WEIXIN.name());

            //1、封装参数
            Map m = new HashMap<>();
            m.put("appid", appid);
            m.put("mch_id", partner);
            m.put("out_trade_no", paymentInfoQuery.getOutTradeNo());
            m.put("nonce_str", WXPayUtil.generateNonceStr());

            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(m, partnerkey));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //6、转成Map
            //7、返回
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
