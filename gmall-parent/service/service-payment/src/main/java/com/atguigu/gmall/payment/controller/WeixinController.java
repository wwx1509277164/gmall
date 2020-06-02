package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.atguigu.gmall.payment.service.WeixinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-02 8:03
 */
@RestController
@RequestMapping("/api/payment/weixin")
@Slf4j
public class WeixinController {
    @Autowired
    private WeixinService weixinPayService;
    @Autowired
    private PaymentInfoService paymentInfoService;

    /**
     * 下单 生成二维码
     *
     * @return
     */
    @GetMapping("/createNative/{orderId}")
    public Result createNative(@PathVariable("orderId") Long orderId) {
        Map map = weixinPayService.createNative(orderId);
        return Result.ok(map);
    }
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable("orderId") Long orderId) {


        return Result.ok();
    }


}
