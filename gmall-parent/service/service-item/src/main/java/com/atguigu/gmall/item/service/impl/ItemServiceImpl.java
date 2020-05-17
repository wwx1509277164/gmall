package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 21:06
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ProductFeignClient productFeignClient;
    @Override
    public Map<String, Object> getItem(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        result.put("skuInfo",skuInfo);
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        result.put("categoryView",categoryView);
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        result.put("price",skuPrice);
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        result.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        return result;
    }
}
