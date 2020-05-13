package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.ManagerService;
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
}
