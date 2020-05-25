package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

/**
 * @author Administrator
 * @create 2020-05-25 18:53
 */
public interface CartService {
    CartInfo addToCart(Long skuId, Integer skuNum, String userId);
}
