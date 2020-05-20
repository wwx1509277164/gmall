package com.atguigu.gmall.all.controller;


import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-19 19:36
 */
@Controller
public class indexController {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    TemplateEngine templateEngine;
/*    @GetMapping("/")
    public String index(Model model){
        List<Map> baseCategoryList = productFeignClient.getBaseCategoryList();
        model.addAttribute("list",baseCategoryList);
        return "index/index";
    }*/
    @GetMapping("/createHtml")
    @ResponseBody
    public void createHtml(){
        Context context = new Context();
        List<Map> baseCategoryList = productFeignClient.getBaseCategoryList();
        context.setVariable("list",baseCategoryList);
        Writer printWriter = null;
        try {
            String templates = ClassUtils.getDefaultClassLoader().getResource("templates").getPath();
            printWriter = new PrintWriter(templates+"/index.html","utf-8");
            templateEngine.process("index/index",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            if (printWriter!=null){
                try {
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @GetMapping("/")
    public String index(){
        return "index";
    }
}
