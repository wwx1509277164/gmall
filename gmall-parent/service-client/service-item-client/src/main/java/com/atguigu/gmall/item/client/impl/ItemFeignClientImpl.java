package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-16 13:39
 */
@Component
public class ItemFeignClientImpl implements ItemFeignClient {
    @Override
    public Map<String, Object> getItem(Long skuId) {
        return null;
    }
}
