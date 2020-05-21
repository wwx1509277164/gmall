package com.atguigu.gmall.product.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.common.util.UuidUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-12 19:02
 */
@Service
public class ManagerServiceImpl implements ManagerService {
    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.attrInfoList(category1Id, category2Id, category3Id);
        return baseAttrInfoList;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo attrInfo) {
        baseAttrInfoMapper.insert(attrInfo);
        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(attrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Override
    public BaseAttrInfo getAttrValueList(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        List<BaseAttrValue> list = baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id", attrId));
        baseAttrInfo.setAttrValueList(list);
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuByPage(Integer page, Integer limit, Long category3Id) {
        IPage<SpuInfo> p = spuInfoMapper.selectPage(new Page<SpuInfo>(), new QueryWrapper<SpuInfo>().eq("category3_id", category3Id));
        return p;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {
        List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null);
        return baseTrademarkList;
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存spuInfo
        spuInfoMapper.insert(spuInfo);
        //保存照片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insert(spuImage);
        }

        //保存属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            //保存属性值
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }

    }

    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        List<SpuImage> spuImageList = spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.spuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    public void updateTradeMark(BaseTrademark baseTrademark) {
        baseTrademarkMapper.updateById(baseTrademark);
    }

    @Override
    public void saveTradeMark(BaseTrademark baseTrademark) {
        baseTrademarkMapper.insert(baseTrademark);
    }

    @Override
    public BaseTrademark getTradeMarkById(Long id) {
        return baseTrademarkMapper.selectById(id);
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //1.保存skuinfo
        skuInfo.setIsSale(0);
        skuInfoMapper.insert(skuInfo);
        //2.保存sku照片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insert(skuImage);
        }
        //3.保存AttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        }

        //保存销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }


    }

    @Override
    public IPage<SkuInfo> getSkuPage(Integer page, Integer limit) {
        return skuInfoMapper.selectPage(new Page<SkuInfo>(page,limit),null);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    public SkuInfo getSkuInfoByRedisson(Long skuId) {
        String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        String skuLock = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
        if (skuInfo!=null){
            return skuInfo;
        }else {
            RLock lock = redissonClient.getLock(skuLock);
            try {
                boolean res = lock.tryLock(1, 3, TimeUnit.SECONDS);
                if (res){
                    skuInfo = skuInfoMapper.selectById(skuId);
                    //6:缓存穿透
                    if (null == skuInfo) {
                        skuInfo = new SkuInfo();
                        redisTemplate.opsForValue().set(skuKey, skuInfo, 5, TimeUnit.MINUTES);
                    } else {
                        //查询图片
                        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                        skuInfo.setSkuImageList(skuImageList);
                        //随机数 解决缓存雪崩问题
                        //缓存一天
                        redisTemplate.opsForValue().set(skuKey, skuInfo,
                                RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    }
                }else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return getSkuInfo(skuId);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
        return skuInfo;
    }
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        String skuLock = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
        SkuInfo result = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
        if (result!=null){
            return result;
        }else {
            String uuid = UUID.randomUUID().toString();
            Boolean isLock = redisTemplate.opsForValue().setIfAbsent(skuLock, uuid,1,TimeUnit.MINUTES);
            if (isLock){
                SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return tostring(redis.call('del',KEYS[1])) else return 0 end";
                if (skuInfo==null){
                    redisTemplate.opsForValue().set(skuKey,skuInfo,5000, TimeUnit.SECONDS);
                    this.redisTemplate.execute(new DefaultRedisScript<>(script), Collections.singletonList(skuLock), uuid);
                    return skuInfo;
                }else {
                    List<SkuImage> imageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                    skuInfo.setSkuImageList(imageList);
                    Random random = new Random();
                    int i = random.nextInt(5000);
                    redisTemplate.opsForValue().set(skuKey, skuInfo,i+RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    //缺乏原子性
                   /* String u = (String) redisTemplate.opsForValue().get(skuLock);
                    if (!StringUtil.isEmpty(u) && u.equals(uuid)) {
                        redisTemplate.delete(skuLock);
                    }*/
                   //使用lua脚本进行删除   保持了原子性
                    this.redisTemplate.execute(new DefaultRedisScript<>(script), Collections.singletonList(skuLock), uuid);
                    return skuInfo;
                }
            }else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfo(skuId);
            }

        }
    }

    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return skuInfoMapper.selectById(skuId).getPrice();
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object,Object> result = new HashMap<>();
        List<Map> mapList = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        for (Map map : mapList) {
            result.put(map.get("value_ids"),map.get("sku_id"));
        }
        return result;
    }


    //查询基本分类视图 所有集合
    @Override
    public List<Map> getBaseCategoryList() {
        //创建返回的list
        List<Map> result = new ArrayList<>();
        //从数据库查询所有的集合，数据结构不符合前台需要的数据结构
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        //设置数据的index
        int index = 1;
        //将所有的数据先按照一级分类进行分组
        Map<Long, List<BaseCategoryView>> category1IdMap = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : category1IdMap.entrySet()) {
            Map map1 = new HashMap<>();
            //插入一级分类的index
            map1.put("index",index++);
            //插入一级分类的名字
            map1.put("categoryName",category1Entry.getValue().get(0).getCategory1Name());
            //插入一级分类的id
            map1.put("categoryId",category1Entry.getValue().get(0).getCategory1Id());
            //插入二级分类
            Map<Long, List<BaseCategoryView>> category2IdMap = category1Entry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            List<Map> category2IdResult = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2IdMap.entrySet()) {
                Map map2 = new HashMap<>();
                map2.put("categoryId",category2Entry.getValue().get(0).getCategory2Id());
                map2.put("categoryName",category2Entry.getValue().get(0).getCategory2Name());
                List<BaseCategoryView> category3List = category2Entry.getValue();
                List<Map> category3IdMap = new ArrayList<>();
                for (BaseCategoryView baseCategoryView : category3List) {
                    HashMap<Object, Object> map3 = new HashMap<>();
                    map3.put("categoryId",baseCategoryView.getCategory3Id());
                    map3.put("categoryName",baseCategoryView.getCategory3Name());
                    category3IdMap.add(map3);
                }
                map2.put("categoryChild",category3IdMap);
                category2IdResult.add(map2);
            }
            map1.put("categoryChild",category2IdResult);
            result.add(map1);
        }
        System.out.println(result);
        return result;
    }

    @Override
    public List<SkuAttrValue> getAttrList(Long skuId) {
        return skuAttrValueMapper.getAttrList(skuId);
    }
}
