package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-12 18:55
 */
@RestController
@RequestMapping("/admin/product")
//@CrossOrigin
public class ManagerController {

    @Autowired
    ManagerService managerService;

    //获取一级分类
    @GetMapping("/getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> baseCategory1List = managerService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    //获取二级分类
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id){
        List<BaseCategory2> baseCategory2List = managerService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }
    //获取三级分类
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id){
        List<BaseCategory3> baseCategory3List = managerService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }
    //获取平台属性
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable("category1Id") Long category1Id,
                               @PathVariable("category2Id") Long category2Id,
                               @PathVariable("category3Id") Long category3Id){
        System.out.println(""+category1Id+" "+category2Id+" "+category3Id);
        List<BaseAttrInfo> baseAttrInfoList = managerService.attrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(baseAttrInfoList);
    }

    //添加平台属性
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        managerService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    //根据平台id属性id获取平台属性
    @PostMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId){
        BaseAttrInfo baseAttrInfo = managerService.getAttrValueList(attrId);
        return Result.ok(baseAttrInfo);
    }


    //获取spu分页列表
    @GetMapping("/{page}/{limit}")
    public Result getSpuByPage(@PathVariable("page") Integer page,
                               @PathVariable("limit") Integer limit,
                               Long category3Id){
        IPage<SpuInfo> p = managerService.getSpuByPage(page,limit,category3Id);
        return Result.ok(p);
    }


    //获取销售属性
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = managerService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    //获取品牌属性
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = managerService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }

    //添加spu
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        managerService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    //根据spuId获取图片列表
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId){
        List<SpuImage> spuImageList = managerService.spuImageList(spuId);
        return Result.ok(spuImageList);
    }

    //根据spuId获取销售属性
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = managerService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    //修改品牌
    @PutMapping("baseTrademark/update")
    public Result UpdateTrademark(BaseTrademark baseTrademark){
        managerService.updateTradeMark(baseTrademark);
        return Result.ok();
    }

    //保存品牌
    @PostMapping("baseTrademark/save")
    public Result SaveTrademark(BaseTrademark baseTrademark){
        managerService.saveTradeMark(baseTrademark);
        return Result.ok();
    }

    //回显品牌
    @GetMapping("baseTrademark/get/{id}")
    public Result getTradeMarkById(@PathVariable("id") Long id){
        BaseTrademark baseTrademark = managerService.getTradeMarkById(id);
        return Result.ok(baseTrademark);
    }


    //保存sku
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        managerService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    //获取sku分页列表
    @GetMapping("/list/{page}/{limit}")
    public Result getSkuPage(@PathVariable("page") Integer page,
                             @PathVariable("limit") Integer limit){
        IPage<SkuInfo> p =  managerService.getSkuPage(page,limit);
        return Result.ok(p);
    }

    //商品的下架
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){
        managerService.cancelSale(skuId);
        return Result.ok();
    }
    //商品的上架
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        managerService.onSale(skuId);
        return Result.ok();
    }

}
