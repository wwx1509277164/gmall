package com.atguigu.gmall.cart.service.Impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-25 18:53
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum, String userId) {
        //查询DB中和redis中查询是否已经存在
        String cacheKey = cacheKey(userId);
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForValue().get(cacheKey);
        if (null==cartInfo){
            cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().eq("user_id"
                    , userId).eq("sku_id", skuId));
        }
        if (null!=cartInfo){
            //再次之前此用户可能添加过该商品了
            cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
            BigDecimal price = productFeignClient.getSkuInfo(skuId).getPrice();
            cartInfo.setSkuPrice(price);
            cartInfo.setIsChecked(1);
            //更新mysql数据库
            cartInfoMapper.updateById(cartInfo);
        }else {
            //添加操作
            cartInfo = new CartInfo();
            //添加数据
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setIsChecked(1);
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            //向数据库里添加数据
            cartInfoMapper.insert(cartInfo);
        }
        //保存redis
        redisTemplate.opsForValue().set(cacheKey,cartInfo);
        setCartKeyExpire(cacheKey);
        return cartInfo;
    }

    private void setCartKeyExpire(String cacheKey) {
        redisTemplate.expire(cacheKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    private String cacheKey(String userId) {
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }
}
