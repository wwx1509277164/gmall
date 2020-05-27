package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Administrator
 * @create 2020-05-25 18:17
 */
@Controller

public class cartController {

    @Autowired
    CartFeignClient cartFeignClient;

    @GetMapping("addCart.html")
    public String addCart(Long skuId, Integer skuNum, Model model){
        CartInfo cartInfo = cartFeignClient.addToCart(skuId, skuNum);
        model.addAttribute("cartInfo",cartInfo);
        return "cart/addCart";
    }

    @GetMapping("/cart.html")
    public String toCart(){
        return "cart/index";
    }
}
