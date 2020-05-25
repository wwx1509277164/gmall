package com.atguigu.gmall.list.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @create 2020-05-21 11:04
 */
@Component
public class ListFeignClientImpl implements ListFeignClient {
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        return null;
    }
}
