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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public List<CartInfo> getCartCheckedList(String userId) {
        //优先从缓存中进行查询
        String cacheKey = this.cacheKey(userId);
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cacheKey);
        if (CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("is_checked",1));
        }else {
            //选中购物车集合
            cartInfoList = cartInfoList.stream().filter((cartInfo) -> {
                if (cartInfo.getIsChecked().intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        }
        cartInfoList.forEach(cartInfo -> {
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
        });
        return cartInfoList;
    }

    @Override
    public void checkCart(Long skuId, Integer isChecked, String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId).eq("sku_id",skuId);
        cartInfoMapper.update(cartInfo,wrapper);
    }

    //查询购物车
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        System.out.println(userId);
        System.out.println(userTempId);
        //1.如果未登录  使用临时用户进行查询
        if (StringUtils.isEmpty(userId)){
            return this.cartList(userTempId);
        }else {
            //如果临时用户不在，就要查询真实用户的购物车
            if (StringUtils.isEmpty(userTempId)){
                return this.cartList(userId);
            }
            return this.mergeCartList(userId,userTempId);
        }
    }

    private List<CartInfo> mergeCartList(String userId, String userTempId) {
        //查询临时和真实用户的购物车
        List<CartInfo> cartInfoListTemp = this.cartList(userTempId);
        List<CartInfo> cartInfoList = this.cartList(userId);
        Map<String, CartInfo> cartInfoMap = cartInfoList.stream().collect(Collectors.toMap(cartInfo -> cartInfo.getSkuId().toString(), cartInfo -> cartInfo));
        for (CartInfo cartInfoTemp : cartInfoListTemp) {
            CartInfo cartInfo = cartInfoMap.get(cartInfoTemp.getSkuId().toString());
            if (cartInfo!=null){
                cartInfo.setSkuNum(cartInfo.getSkuNum()+cartInfoTemp.getSkuNum());
                //操作DB
                if (cartInfoTemp.getIsChecked().intValue()==1){
                    cartInfo.setIsChecked(1);
                }
                cartInfoMapper.updateById(cartInfo);
                cartInfoMapper.deleteById(cartInfoTemp);
            }else {
                cartInfoMap.put(cartInfoTemp.getSkuId().toString(),cartInfoTemp);
                cartInfoTemp.setUserId(userId);
                cartInfoMapper.updateById(cartInfoTemp);
            }
        }
        //统一删除临时用户缓存中的数据
        String cacheKeyTemp = this.cacheKey(userTempId);
        String cacheKey = this.cacheKey(userId);
        if (redisTemplate.hasKey(cacheKeyTemp)){
            //删除整个hashmap
            redisTemplate.delete(cacheKeyTemp);
            redisTemplate.delete(cacheKey);
        }
        //返回结果集
        return new ArrayList<CartInfo>(cartInfoMap.values());
    }

    private List<CartInfo> cartList(String userTempId) {
        //先从缓存中查询数据
        String cacheKey = cacheKey(userTempId);
        List<CartInfo> list = redisTemplate.opsForHash().values(cacheKey);
        if (CollectionUtils.isEmpty(list)){
            //如果缓存中没有，从数据库进行查询
            list = loadCartCache(userTempId);
        }
        //查询实时的价格
        list.forEach(cartInfo -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
            cartInfo.setCartPrice(skuPrice);
            cartInfo.setSkuPrice(skuPrice);
        });
        return list;
    }

    public List<CartInfo> loadCartCache(String userTempId){
        String cacheKey = cacheKey(userTempId);
        List<CartInfo>  list = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userTempId));
        if (!CollectionUtils.isEmpty(list)) {
            //存放到缓存当中
            Map<String, CartInfo> map = list.stream().collect(Collectors.toMap(cartInfo -> cartInfo.getSkuId().toString(), cartInfo -> cartInfo));
            redisTemplate.opsForHash().putAll(cacheKey,map);
            setCartKeyExpire(cacheKey);
        }
        return list;
    }
    //添加购物车
    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum, String userId) {
        //查询DB中和redis中查询是否已经存在
        String cacheKey = cacheKey(userId);
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cacheKey,skuId.toString());
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
        loadCartCache(userId);
        return cartInfo;
    }

    private void setCartKeyExpire(String cacheKey) {
        redisTemplate.expire(cacheKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    private String cacheKey(String userId) {
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }
}
