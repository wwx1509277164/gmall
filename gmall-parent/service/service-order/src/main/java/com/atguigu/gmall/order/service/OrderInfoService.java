package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-28 8:58
 */
public interface OrderInfoService {
    boolean hasStock(Long skuId,Integer skuNum);

    Long submitOrder(OrderInfo orderInfo);

    void cancelOrder(Long orderId);

    OrderInfo getOrderInfo(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus finished, ProcessStatus paid);

    void sendOrderStatus(Long orderId);

    public void updateOrderStatus(Long orderId, ProcessStatus paid);
    public Map<String, Object> initWareOrder(OrderInfo orderInfo);
    public String initWareOrder(Long orderId);

    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
