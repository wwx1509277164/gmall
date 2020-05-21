package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.impl.ListFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Administrator
 * @create 2020-05-21 11:02
 */
@FeignClient(name = "service-list",fallback = ListFeignClientImpl.class)
public interface ListFeignClient {
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId);
}
