package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-20 9:40
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    GoodsDao goodsDao;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //添加sku
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setId(skuId);
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());

        //添加品牌信息
        BaseTrademark baseTrademark = productFeignClient.getBaseTrademark(skuInfo.getTmId());
        goods.setTmId(skuInfo.getTmId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        //添加123级分类
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());
        //添加平台属性集合
        List<SkuAttrValue> attrList = productFeignClient.getAttrList(skuId);
        System.out.println(attrList);
        List<SearchAttr> searchAttrList = attrList.stream().map(skuAttrValue -> {
            SearchAttr searchAttr = new SearchAttr();
            //平台属性id
            searchAttr.setAttrId(skuAttrValue.getBaseAttrInfo().getId());
            //平台属性名称
            searchAttr.setAttrName(skuAttrValue.getBaseAttrInfo().getAttrName());
            //平台属性值名称
            searchAttr.setAttrValue(skuAttrValue.getBaseAttrValue().getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);
        goodsDao.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsDao.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        String hotScore = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotScore, skuId, 1);
        System.out.println(score);
        if (score%10==0){
            Optional<Goods> optional = goodsDao.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(score));
            goodsDao.save(goods);
        }
    }

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    RestHighLevelClient restHighLevelClient;
    //建议使用原生api
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        SearchResponseVo searchResponseVo = null;
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
           searchResponseVo = parseSearchResponse(searchResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponseVo;
    }

    //解析结果
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        //查询总条数
        long totalHits = hits.getTotalHits();
        searchResponseVo.setTotal(totalHits);
        //查询商品结果集
        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hits1).map(h -> {
            String sourceAsString = h.getSourceAsString();
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            return goods;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);
        return searchResponseVo;
    }
    //构建条件对象
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        //构建条件资源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //关键词
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //123级分类
        //品牌
        //平台属性
        //排序
        //分页
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        //在7.0之后会取消这个type
        //searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }


}
