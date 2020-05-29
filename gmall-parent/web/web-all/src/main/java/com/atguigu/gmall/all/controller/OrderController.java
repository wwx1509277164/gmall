package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-27 9:03
 */
@Controller
public class OrderController {
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    UserFeignClient userFeignClient;
    @Autowired
    OrderFeignClient orderFeignClient;
    @GetMapping("/trade.html")
    public String trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        //查询收货地址集合
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        //查询商品清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        List<OrderDetail> orderDetailList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cartInfo, orderDetail);
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            return orderDetail;
        }).collect(Collectors.toList());
        //查询数量，商品的总金额
        long totalNum = cartCheckedList.stream().collect(Collectors.summarizingInt(CartInfo::getSkuNum)).getSum();
        Double totalPrice = cartCheckedList.stream().collect(Collectors.summarizingDouble(cartInfo -> {
            return cartInfo.getSkuNum() * cartInfo.getSkuPrice().doubleValue();
        })).getSum();

        //
        String tradeNo = orderFeignClient.trade();
        request.setAttribute("totalNum",totalNum);
        request.setAttribute("totalAmount",totalPrice);
        request.setAttribute("userAddressList",userAddressList);
        request.setAttribute("detailArrayList",orderDetailList);
        request.setAttribute("tradeNo",tradeNo);
        return "order/trade";
    }
}
