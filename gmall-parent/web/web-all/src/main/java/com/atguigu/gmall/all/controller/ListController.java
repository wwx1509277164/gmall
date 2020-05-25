package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-22 19:42
 */
@Controller
public class ListController {

    @Autowired
    ListFeignClient listFeignClient;

    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model){
        //将入参对象进行回显
        model.addAttribute("searchParam",searchParam);
        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);
        model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
        model.addAttribute("attrsList",searchResponseVo.getAttrsList());
        model.addAttribute("goodsList",searchResponseVo.getGoodsList());
        model.addAttribute("pageNo",searchResponseVo.getPageNo());
        System.out.println(searchResponseVo.getPageNo());
        model.addAttribute("totalPages",searchResponseVo.getTotalPages());
        System.out.println(searchResponseVo.getTotal());
        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        //排序 显示红色底  建呕吐由高到低或是由低到高
        Map orderMap = makeOrderMap(searchParam);
        model.addAttribute("orderMap",orderMap);
        //品牌回显
        String trademarkParam = makeTrademarkParam(searchParam);
        model.addAttribute("trademarkParam",trademarkParam);
        //回显平台属性
        List<Map> propsParamList = makeParamList(searchParam);
        model.addAttribute("propsParamList",propsParamList);
        return "list/index";
    }

    private List<Map> makeParamList(SearchParam searchParam) {
        String[] props = searchParam.getProps();
        if (props!=null&&props.length>0){
            return Arrays.stream(props).map(prop -> {
                String[] p = prop.split(":");
                Map<Object, Object> map = new HashMap<>();
                map.put("attrName", p[2]);
                map.put("attrId", p[0]);
                map.put("attrValue", p[1]);
                return map;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private String makeTrademarkParam(SearchParam searchParam) {
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            String trademarkParam = "品牌:" +split[1];
            return trademarkParam;
        }
        return null;
    }

    private Map makeOrderMap(SearchParam searchParam) {
        Map map = new HashMap<>();
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            String[] o = order.split(":");
            map.put("type",o[0]);
            map.put("sort",o[1]);
        }else {
            map.put("type","1");
            map.put("sort","desc");
        }
        return map;
    }

    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder sb = new StringBuilder();
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)){
            sb.append("keyword=").append(keyword);
        }
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            if (sb.length()>0) {
                sb.append("&trademark=").append(trademark);
            }else {
                sb.append("trademark=").append(trademark);
            }
        }
        Long category1Id = searchParam.getCategory1Id();
        if (category1Id!=null){
            if (sb.length()>0) {
                sb.append("&category1Id=").append(category1Id);
            }else {
                sb.append("category1Id=").append(category1Id);
            }
        }
        Long category2Id = searchParam.getCategory2Id();
        if (category2Id!=null){
            if (sb.length()>0) {
                sb.append("&category2Id=").append(category2Id);
            }else {
                sb.append("category2Id=").append(category2Id);
            }
        }
        Long category3Id = searchParam.getCategory3Id();
        if (category3Id!=null){
            if (sb.length()>0) {
                sb.append("&category3Id=").append(category3Id);
            }else {
                sb.append("category3Id=").append(category3Id);
            }
        }

        //平台属性
        String[] props = searchParam.getProps();
        if (props!=null&&props.length>0){
            for (String prop : props) {
                if (sb.length()>0){
                    sb.append("&props=").append(prop);
                }else {
                    sb.append("props=").append(prop);
                }
            }
        }

        return "/list.html?"+sb.toString();
    }
}
