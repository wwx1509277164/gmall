package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @GetMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    CartInfo addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum);


    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId);
}
