package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * @author Administrator
 * @create 2020-05-28 8:58
 */
public interface OrderInfoService {
    boolean hasStock(Long skuId,Integer skuNum);

    Long submitOrder(OrderInfo orderInfo);

    void cancelOrder(Long orderId);

    OrderInfo getOrderInfo(Long orderId);
}
