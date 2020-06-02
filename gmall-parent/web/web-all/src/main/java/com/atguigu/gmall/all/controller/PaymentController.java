package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-06-01 7:54
 */
@Controller
public class PaymentController {
    @Autowired
    OrderFeignClient orderFeignClient;
    @GetMapping("/pay.html")
    public String pay(Long orderId, Model model){
        //远程调用微服务
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        model.addAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    @GetMapping("/pay/success.html")
    public String success(){
        return "payment/success";
    }

    @GetMapping("weixin.html")
    public String weixin(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        request.setAttribute("orderId", orderId);
        return "payment/weixin";
    }

}
