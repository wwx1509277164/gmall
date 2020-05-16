package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 21:03
 */
@Api("商品详情页面数据查询")
@RestController
@RequestMapping("/api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;


    @GetMapping("/getItem/{skuId}")
    public Map<String,Object> getItem(@PathVariable("skuId") Long skuId){

        Map<String,Object> map = itemService.getItem(skuId);

        return map;
    }

}
