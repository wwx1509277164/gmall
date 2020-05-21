package com.atguigu.gmall.list.dao;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @create 2020-05-20 9:16
 */
@Component
public interface GoodsDao extends ElasticsearchRepository<Goods,Long> {
}
