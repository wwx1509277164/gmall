package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-29 18:51
 */
//封装发送消息  MQ
@Component
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    public void sendMessage(String exchange,String routingKey,Object msg){
        //交换机应答疯转对象    失败了之后的封装的信息
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String id = UUID.randomUUID().toString().replace("-", "");
        gmallCorrelationData.setId(id);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(msg);
        String jsonString = JSONObject.toJSONString(gmallCorrelationData);
        redisTemplate.opsForValue().set(gmallCorrelationData.getId(),jsonString,1, TimeUnit.MINUTES);
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,gmallCorrelationData);
    }
    //发送延迟消息
    public void sendDelayedMessage(String exchange, String routingKey, Object msg, int delayTime) {
        //封装交换机 应答返回对象
        GmallCorrelationData correlationData = new GmallCorrelationData();
        //主键
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        correlationData.setId(id);
        System.out.println("发送消息的时候的主键：" + id);
        //交换机
        correlationData.setExchange(exchange);
        //RoutingKey
        correlationData.setRoutingKey(routingKey);
        //消息体
        correlationData.setMessage(msg);
        //设置 是延迟消息
        correlationData.setDelay(true);
        //延迟时间  设置新的延迟消息
        correlationData.setDelayTime(delayTime);
        //为了防止 队列接收消息失败 应答的时候 需要使用到下面缓存信息
        redisTemplate.opsForValue().set(id, JSONObject.toJSONString(correlationData), 5, TimeUnit.MINUTES);
        //发消息 设置延迟的时间
        rabbitTemplate.convertAndSend(exchange, routingKey,
                msg, (message) -> {
                    message.getMessageProperties().setDelay(correlationData.getDelayTime());
                    System.out.println("发送延迟消息的时间：" + new Date());
                    return message;
                }, correlationData);
    }
}
