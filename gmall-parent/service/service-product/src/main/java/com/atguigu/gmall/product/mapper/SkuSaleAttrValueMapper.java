package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 8:09
 */
@Mapper
public interface SkuSaleAttrValueMapper  extends BaseMapper<SkuSaleAttrValue> {
    List<Map> getSkuValueIdsMap(@Param("spuId") Long spuId);
}
