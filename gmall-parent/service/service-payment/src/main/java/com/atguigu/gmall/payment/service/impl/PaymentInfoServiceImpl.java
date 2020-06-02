package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.OrderInfoMapper;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 13:54
 */
@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Override
    public PaymentInfo savaPaymentInfo(Long orderId, PaymentType type) {
        //判断支付信息表是否已经保存过了  防止二次提交
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("order_id", orderId));
        if (null==paymentInfo){
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setSubject(orderInfo.getTradeBody());
            paymentInfo.setOrderId(orderId);
            //支付类型
            paymentInfo.setPaymentType(type.name());
            paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
            paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
            paymentInfo.setCreateTime(new Date());
            paymentInfoMapper.insert(paymentInfo);
        }
        return paymentInfo;
    }

    @Override
    public void paySuccess(Map<String, String> paramsMap) {
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("out_trade_no"
                , paramsMap.get("out_trade_no")));
        if (PaymentStatus.UNPAID.name().equals(paymentInfo.getPaymentStatus())){
            //更新支付状态
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(JSONObject.toJSONString(paramsMap));
            paymentInfoMapper.updateById(paymentInfo);
        }
    }

}
