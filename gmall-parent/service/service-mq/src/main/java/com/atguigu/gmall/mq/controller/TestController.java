package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

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

    @Autowired
    private RabbitTemplate rabbitTemplate;
    //基于死信的延迟消息
        @GetMapping("sendDeadLetterMessage")
    public Result sendDeadLetterMessage(){

        rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead,
                DeadLetterMqConfig.routing_dead_1,"11111",
                (message)->{
                    message.getMessageProperties().setExpiration("8000");
                    System.out.println("发送延迟消息："+":"+new Date());
                    return message;
                });
        return Result.ok();
    }
    //基于插件延迟消息的发送测试
    @GetMapping("sendDelayMessage")
    public Result sendDelayMessage(){

        rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay,
                DelayedMqConfig.routing_delay,"111fdsafdsaf11",
                (message)->{
                    //延迟多久
                    System.out.println("发送延迟消息："+":"+new Date());
                    message.getMessageProperties().setDelay(10000);
                    return message;
                });
        return Result.ok();
    }
}
