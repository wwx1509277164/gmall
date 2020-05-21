package com.atguigu.gmall.product.api;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 16:57
 */
@RestController
@RequestMapping("/api/product")
public class ProductApiController {
    @Autowired
    ManagerService managerService;

    //获取sku的基本信息
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId){
       return  managerService.getSkuInfo(skuId);
    }

    //获取三级分类信息
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id){
        return managerService.getCategoryView(category3Id);
    }

    //获取价格信息
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        return managerService.getSkuPrice(skuId);
    }

    //查询所有的销售属性和销售属性值，包括当前sku的属性值
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId,
                                                          @PathVariable("spuId") Long spuId){
        return managerService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    //获取销售属性值的集合
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return managerService.getSkuValueIdsMap(spuId);
    }


    @GetMapping("/getBaseCategoryList")
    public List<Map> getBaseCategoryList(){
        return managerService.getBaseCategoryList();
    }
    @GetMapping("/getBaseTrademark/{id}")
    public BaseTrademark getBaseTrademark(@PathVariable("id") Long id){
        return managerService.getTradeMarkById(id);
    }

    @GetMapping("/getAttrList/{skuId}")
    public List<SkuAttrValue> getAttrList(@PathVariable("skuId") Long skuId){
        return managerService.getAttrList(skuId);
    }
}
