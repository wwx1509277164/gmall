package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.LongTermsAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import javax.swing.text.Highlighter;
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
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果
            searchResponseVo = parseSearchResponse(searchResponse);
            //设置返回值
            searchResponseVo.setPageNo(searchParam.getPageNo());
            searchResponseVo.setPageSize(searchParam.getPageSize());
            Long totalPages =  (searchResponseVo.getTotal() + searchResponseVo.getPageSize()-1)/searchResponseVo.getPageSize();
            searchResponseVo.setTotalPages(totalPages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponseVo;
    }

    //解析结果
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        //查询总条数   通过hits可以查询总条数和所有的结果集
        long totalHits = hits.getTotalHits();
        searchResponseVo.setTotal(totalHits);
        //查询商品结果集
        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hits1).map(h -> {
            String sourceAsString = h.getSourceAsString();
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            HighlightField title = h.getHighlightFields().get("title");
            if (title!=null){
                String t = title.fragments()[0].toString();
                goods.setTitle(t);
            }
            return goods;
        }).collect(Collectors.toList());
        //3: 构建条件的时候 设置了品牌分组  解析品牌分组结果
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) searchResponse.getAggregations().asMap().get("tmIdAgg");
        List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo responseTmVo = new SearchResponseTmVo();
            //设置id
            responseTmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));
            //设置name
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            //设置url
            responseTmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            responseTmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
            return responseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(searchResponseTmVoList);

        //4:构建条件的时候 设置了平台属性分组 解析平台属性分组结果
        ParsedNested attrsAgg = (ParsedNested) searchResponse.getAggregations().asMap().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            searchResponseAttrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> stringList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(stringList);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(searchResponseAttrVoList);
        searchResponseVo.setGoodsList(goodsList);
        return searchResponseVo;
    }
    //构建条件对象
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        //构建条件资源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建条件对象，因为资源对象只能使用一次过滤，所以采用条件对象将所有的条件放到里面
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //关键词
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            //matchQuery进行分词查询，termQuery不进行分词查询
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        }
        //searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //123级分类
        Long category1Id = searchParam.getCategory1Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category3Id = searchParam.getCategory3Id();
        if (category1Id!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",category1Id));
        }
        if (category2Id!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",category2Id));
        }
        if (category3Id!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",category3Id));
        }
        //品牌
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            String[] t = trademark.split(":");
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",t[0]));
        }
        //平台属性    平台属性里面的值进行过滤跟其他的不是一个级别的，所以进行子过滤
        String[] props = searchParam.getProps();
        if (props!=null&&props.length!=0){
            for (String prop : props) {
                //需要new一个子查询条件，然后将子查询条件放入父查询条件里面
                BoolQueryBuilder attrBoolQueryBuilder = QueryBuilders.boolQuery();
                String[] p = prop.split(":");
                attrBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",p[0]));
                attrBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",p[1]));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs",attrBoolQueryBuilder, ScoreMode.None));
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            String[] o = order.split(":");
            String fieldName = "";
            switch (o[0]){
                case "1":fieldName="hotScore";break;
                case "2":fieldName="price";break;
            }
            searchSourceBuilder.sort(fieldName, o[1].equals("asc")?SortOrder.ASC:SortOrder.DESC);
        }else {
            searchSourceBuilder.sort("hotScore",SortOrder.ASC);
        }
        //分页
        searchSourceBuilder.from((searchParam.getPageNo()-1)*searchParam.getPageSize());
        searchSourceBuilder.size(searchParam.getPageSize());

        //隐藏条件高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").preTags("<font color='red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //设置品牌分组
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                            .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                            .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));

        //设置平台属性分组
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg","attrs")
                            .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                            .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        //在7.0之后会取消这个type
        //searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }


}
