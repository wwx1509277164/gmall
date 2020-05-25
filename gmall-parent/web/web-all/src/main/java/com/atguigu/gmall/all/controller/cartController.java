package com.atguigu.gmall.all.controller;

import io.swagger.models.auth.In;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Administrator
 * @create 2020-05-25 18:17
 */
@Controller

public class cartController {

    @GetMapping("addCart.html")
    public String addCart(Long skuId, Integer skuNum, Model model){
        model.addAttribute("cartInfo",null);
        return "cart/addCart";
    }
}
