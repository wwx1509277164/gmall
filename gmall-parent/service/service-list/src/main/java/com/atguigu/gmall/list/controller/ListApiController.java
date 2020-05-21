package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author Administrator
 * @create 2020-05-19 21:52
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {

    @Autowired
    ListService listService;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    //索引库
    @GetMapping("/index")
    public Result index(){
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    //上架商品 保存索引库
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId){
        listService.upperGoods(skuId);
        return Result.ok();
    }
    //下架商品 删除索引库
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId){
        listService.lowerGoods(skuId);
        return Result.ok();
    }

    //更新商品的热度
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId){
        listService.incrHotScore(skuId);
        return Result.ok();
    }

    //开始执行搜索
    @PostMapping
    public SearchResponseVo list(@RequestBody SearchParam searchParam){

        return listService.list(searchParam);
    }

}
