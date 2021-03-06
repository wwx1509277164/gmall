package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-25 18:22
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    CartService cartService;

    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable("skuId") Long skuId,
                              @PathVariable("skuNum") Integer skuNum,
                              HttpServletRequest request){
        //用户ID
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        System.out.println("skuId:"+skuId);
        System.out.println("skuNum:"+skuNum);
        System.out.println("userId"+userId);
        return cartService.addToCart(skuId,skuNum,userId);
    }


    //去购物车结算
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.cartList(userId,userTempId);
        return Result.ok(cartInfoList);
    }

    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("isChecked") Integer isChecked,
                            HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        cartService.checkCart(skuId,isChecked,userId);
        return Result.ok();
    }


    //查询选中的购物车的集合
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId){
        return cartService.getCartCheckedList(userId);
    }
}




