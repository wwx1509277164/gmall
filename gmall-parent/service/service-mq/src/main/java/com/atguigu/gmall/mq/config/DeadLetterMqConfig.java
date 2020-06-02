package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-31 17:53
 */
//java配置类
@Configuration
public class DeadLetterMqConfig {
    //交换机
    public static final String exchange_dead = "exchange.dead";
    //routingKey
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    //两个队列
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";



    @Bean
    public DirectExchange directExchange(){
        return ExchangeBuilder.directExchange(exchange_dead).build();
    }

    @Bean
    public Queue queue1(){
        Map arguments = new HashMap<String,Object>();
        //设置过期转发的交换机和交换机队列和过期的时间
        arguments.put("x-dead-letter-exchange",exchange_dead);
        arguments.put("x-dead-letter-routing-key",routing_dead_2);
        arguments.put("x-message-ttl",10*1000);
        return QueueBuilder.durable(queue_dead_1).withArguments(arguments).build();
    }


    @Bean
    public Queue queue2(){
        return QueueBuilder.durable(queue_dead_2).build();
    }

    @Bean
    public Binding bindingQueue1(){
        return BindingBuilder.bind(queue1()).to(directExchange()).with(routing_dead_1);
    }

    @Bean
    public Binding bindingQueue2(){
        return BindingBuilder.bind(queue2()).to(directExchange()).with(routing_dead_2);
    }




}
