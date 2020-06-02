package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.CartInfoMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.http.annotation.Obsolete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-28 8:59
 */
@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Value("${ware.url}")
    String wareUrl;
    @Override
    public boolean hasStock(Long skuId,Integer skuNum) {
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    @Transactional
    public Long submitOrder(OrderInfo orderInfo) {
        //订单表的保存
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        String outTradeNo = "ATGUIGU"+System.currentTimeMillis();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            outTradeNo += random.nextInt(10);
        }
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setTradeBody("王文新商店");
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfo.sumTotalAmount();
        //orderInfo.setImgUrl();
        orderInfoMapper.insert(orderInfo);
        //订单详情表的操作

        String cacheKey = cacheKey(orderInfo.getUserId().toString());
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);

            //删除购物车里的信息
            //cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id",orderInfo.getUserId()
            //).eq("sku_id",orderDetail.getSkuId()));
            //删除缓存中的数据
            //redisTemplate.opsForHash().delete(cacheKey,orderDetail.getSkuId().toString());
        }
        //发送延迟消息  为了用户不买单的时候 2个小时 就取消此订单
        /*rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL,orderInfo.getId(),MqConst.DELAY_TIME*1000);*/
        return orderInfo.getId();
    }
    private String cacheKey(String userId){

        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }

    @Override
    public void cancelOrder(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus())){
            orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
            orderInfo.setProcessStatus(ProcessStatus.CLOSED.name());
            orderInfoMapper.updateById(orderInfo);
        }
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }


    //更新进度和订单的的状态
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus finished, ProcessStatus paid) {
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        orderInfo.setOrderStatus(finished.name());
        orderInfo.setProcessStatus(paid.name());
        orderInfoMapper.updateById(orderInfo);
    }
    //更新进度状态
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus paid) {
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        orderInfo.setProcessStatus(paid.name());
        orderInfoMapper.updateById(orderInfo);
    }
    //通知库存进行扣减库存
    @Override
    public void sendOrderStatus(Long orderId) {
        this.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        String result = initWareOrder(orderId);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK,result);
    }

    @Override
    public String initWareOrder(Long orderId) {
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        Map<String, Object> result = initWareOrder(orderInfo);
        return JSONObject.toJSONString(result);
    }

    @Override
    public Map<String, Object> initWareOrder(OrderInfo orderInfo) {
        Map<String, Object> result =new HashMap<>();

        result.put("orderId",orderInfo.getId());
        result.put("consignee",orderInfo.getConsignee());
        result.put("consigneeTel",orderInfo.getConsigneeTel());
        result.put("orderComment",orderInfo.getOrderComment());
        result.put("orderBody",orderInfo.getTradeBody());
        result.put("deliveryAddress",orderInfo.getDeliveryAddress());
        result.put("paymentWay","2");
        result.put("wareId",orderInfo.getWareId());
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            List<Map> listMap = orderDetailList.stream().map(orderDetail -> {
                Map map = new HashMap();
                map.put("skuId", orderDetail.getSkuId());
                map.put("skuNum", orderDetail.getSkuNum());
                map.put("skuName", orderDetail.getSkuName());
                return map;
            }).collect(Collectors.toList());
            result.put("details",listMap);
        }
        return result;
    }

    //开始进行拆单操作
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        //原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(Long.parseLong(orderId));
        List<Map> wareSkuMapList = JSONObject.parseArray(wareSkuMap, Map.class);
        List<OrderDetail> orderDetailListOrigin = orderInfoOrigin.getOrderDetailList();
        List<OrderInfo> orderInfoList = new ArrayList<>();
        for (Map wareMap : wareSkuMapList) {
            //设置订单
            OrderInfo orderInfo = new OrderInfo();
            BeanUtils.copyProperties(orderInfoOrigin,orderInfo);
            orderInfo.setId(null);
            orderInfo.setParentOrderId(orderInfoOrigin.getId());
            String wareId = (String) wareMap.get("wareId");
            orderInfo.setWareId(wareId);
            //设置订单详情页面
            List<String> skuIds = (List<String>) wareMap.get("skuIds");
            List<OrderDetail> orderDetails = orderDetailListOrigin.stream().filter(orderDetail -> {
                for (String skuId : skuIds) {
                    if (skuId.equals(orderDetail.getSkuId().toString())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());

            orderInfo.setOrderDetailList(orderDetails);
            //保存订单和订单详情表
            saveOrderInfo(orderInfo);
            //追加到集合当中
            orderInfoList.add(orderInfo);
        }
        orderInfoOrigin.setProcessStatus(ProcessStatus.SPLIT.name());
        this.updateOrderStatus(orderInfoOrigin.getId(),
                OrderStatus.SPLIT,ProcessStatus.SPLIT);
        return orderInfoList;
    }

    private void saveOrderInfo(OrderInfo orderInfo) {
        orderInfoMapper.insert(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
    }
}
