package com.atguigu.gmall.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-05-27 13:36
 */
@FeignClient(value = "service-order")
public interface OrderFeignClient {
    @GetMapping("api/order/auth/trade")
    public String trade();
}
