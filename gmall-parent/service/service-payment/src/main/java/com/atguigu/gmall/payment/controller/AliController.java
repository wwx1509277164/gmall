package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 13:33
 */
@Controller
@RequestMapping("/api/payment/alipay")
public class AliController {

    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentInfoService paymentInfoService;

    @ResponseBody
    @GetMapping("/submit/{orderId}")
    public String submit(@PathVariable("orderId")Long orderId){
        return alipayService.submit(orderId);
    }

    @GetMapping("/callback/return")
    public String callbackReturn(){

        return "redirect:"+AlipayConfig.return_order_url;
    }

    //支付宝支付成功之后的异步的路径
    @ResponseBody
    @PostMapping("callback/notify")
    public String callbackNotify(@RequestParam Map<String,String> paramsMap) throws AlipayApiException {
        boolean singVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key,
                AlipayConfig.charset, AlipayConfig.sign_type);
        if (singVerified){
            System.out.println(paramsMap);
           //{gmt_create=2020-06-01 20:51:55, charset=utf-8, gmt_payment=202
            // 0-06-01 20:52:16, notify_time=2020-06-01 20:52:17, subject=王文新商店, buye
            // r_id=2088102181120093, invoice_amount=23996.00, version=1.0,
            // notify_id=2020060100222205216020090505405125,
            // fund_bill_list=[{"amount":"23996.00","fundChannel":"ALIPAYACCOUNT"}],
            // notify_type=trade_status_sync, out_trade_no=ATGUIGU1591015745196408,
            // total_amount=23996.00, trade_status=TRADE_SUCCESS, trade_no=2020060122001420090500528785,
            // auth_app_id=2016102400749311, receipt_amount=23996.00,
            // point_amount=0.00, app_id=2016102400749311,
            // buyer_pay_amount=23996.00, seller_id=2088102180848804}
            if ("TRADE_SUCCESS".equals(paramsMap.get("trade_status"))){
                paymentInfoService.paySuccess(paramsMap);
            }else {
                return "failure";
            }
            return "success";
        }else {
            return "failure";
        }
    }

    @ResponseBody
    @GetMapping("/refund/{outTradeNo}")
    public Result refund(@PathVariable("outTradeNo") String outTradeNo){
    alipayService.refund(outTradeNo);

        return Result.ok();
    }
}
