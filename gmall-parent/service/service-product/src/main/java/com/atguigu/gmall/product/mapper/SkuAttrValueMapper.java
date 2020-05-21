package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-15 8:09
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
    List<SkuAttrValue> getAttrList(Long skuId);
}
