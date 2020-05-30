package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 * @create 2020-05-29 14:15
 */
@RestController

public class TestController {
    @Autowired
    private RabbitService rabbitService;
    @GetMapping("/sendMessage")
    public Result sendMessage(){
        rabbitService.sendMessage("exchange11","routingKey11","haha");
        return Result.ok();
    }
}
