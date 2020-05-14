package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-12 19:01
 */
public interface ManagerService {
    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo attrInfo);

    BaseAttrInfo getAttrValueList(Long attrId);

    IPage<SpuInfo> getSpuByPage(Integer page, Integer limit, Long category3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    List<BaseTrademark> getTrademarkList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> spuImageList(Long spuId);

    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    void updateTradeMark(BaseTrademark baseTrademark);

    void saveTradeMark(BaseTrademark baseTrademark);

    BaseTrademark getTradeMarkById(Long id);
}
