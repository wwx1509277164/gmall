package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

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
            cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id",orderInfo.getUserId()
            ).eq("sku_id",orderDetail.getSkuId()));
            //删除缓存中的数据
            redisTemplate.opsForHash().delete(cacheKey,orderDetail.getSkuId().toString());
        }

        return orderInfo.getId();
    }
    private String cacheKey(String userId){
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }
}
