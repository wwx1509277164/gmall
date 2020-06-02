package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.StreamUtil;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.atguigu.gmall.payment.service.WeixinService;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
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
        //调用查询接口
        Map<String, String> resultMap = weixinPayService.queryPayStatus(orderId, PaymentType.WEIXIN.name());
        if (resultMap == null) {//出错
            return Result.fail().message("支付出错");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {//如果成功
            //更改订单状态
            //weixinPayService.updateOrderStatus(map);
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no,PaymentType.WEIXIN.name(), resultMap);
            return Result.ok().message("支付成功");
        }

        return Result.build(null,ResultCodeEnum.PAY_RUN);
    }
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    @ApiOperation(value = "微信支付|支付回调接口", httpMethod = "POST", notes = "该链接是通过【统一下单API】中提交的参数notify_url设置，如果链接无法访问，商户将无法接收到微信通知。")
    @RequestMapping("/notify")
    public void wxnotify(HttpServletRequest request, HttpServletResponse response) {
        String resXml = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[报文为空]]></return_msg></xml>";
        try {
            String xmlString = StreamUtil.inputStream2String(request.getInputStream(), "utf-8");
            log.info("wxnotify:微信支付----result----=" + xmlString);

            // xml转换为map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlString);
            if (WXPayUtil.isSignatureValid(resultMap, partnerkey)) {
                log.info("wxnotify:微信支付----返回成功");
                if (WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                    //更改订单状态
                    //weixinPayService.updateOrderStatus(resultMap);
                    String out_trade_no = resultMap.get("out_trade_no");
                    paymentInfoService.paySuccess(out_trade_no, PaymentType.WEIXIN.name(), resultMap);

                    log.info("wxnotify:微信支付----验证签名成功");

                    // 通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                    resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
                } else {
                    log.error("wxnotify:支付失败,错误信息：" + resultMap.get("err_code_des"));
                }
            } else {
                log.error("wxnotify:微信支付----判断签名错误");
            }
        } catch (Exception e) {
            log.error("wxnotify:支付回调发布异常：", e);
        } finally {
            try {
                // 处理业务完毕
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                log.error("wxnotify:支付回调发布异常:out：", e);
            }
        }

    }
}
