package com.atguigu.gmall.common.service;

import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Administrator
 * @create 2020-05-29 18:51
 */
//封装发送消息  MQ
@Component
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void sendMessage(String exchange,String routingKey,Object msg){
        //交换机应答疯转对象    失败了之后的封装的信息
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String id = UUID.randomUUID().toString().replace("-", "");
        gmallCorrelationData.setId(id);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(msg);

        rabbitTemplate.convertAndSend(exchange,routingKey,msg,gmallCorrelationData);
    }
}
