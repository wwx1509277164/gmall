package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * @author Administrator
 * @create 2020-05-27 13:30
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    OrderInfoService orderInfoService;
    @GetMapping("auth/trade")
    public String trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeNo = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);
        return tradeNo;
    }

    //提交我们的订单
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,String tradeNo
                            ,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(tradeNo)){
            return Result.fail().message("非法请求");
        }
        String tradeNoKey = "user:"+userId+":tradeCode";
        String no = (String) redisTemplate.opsForValue().get(tradeNoKey);
        if (StringUtils.isEmpty(no)){
            return Result.fail().message("请不要重复提交订单");
        }else {
            if (!no.equals(tradeNo)){
                return Result.fail().message("非法操作");
            }
        }
        //删除交易号
        redisTemplate.delete(tradeNoKey);
        //查询库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean hasStock =  orderInfoService.hasStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            if (!hasStock){
                return Result.fail().message(orderDetail.getSkuName()+":库存不足");
            }
        }
        orderInfo.setUserId(Long.parseLong(userId));
        //保存订单
        Long orderId =  orderInfoService.submitOrder(orderInfo);
        return Result.ok(orderId);
    }
}
