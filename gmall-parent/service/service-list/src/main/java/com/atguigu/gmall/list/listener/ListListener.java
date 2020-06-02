package com.atguigu.gmall.list.listener;

import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.list.service.ListService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Administrator
 * @create 2020-05-31 9:35
 */
@Component
public class ListListener {
    @Autowired
    private ListService listService;
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_GOODS),
            key = MqConst.ROUTING_GOODS_UPPER)
    })
    public void upperGoods(Long skuId, Message message, Channel channel){
        try {
            System.out.println("上架商品的索引库："+skuId);
            listService.upperGoods(skuId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,durable = "true",autoDelete = "false"),
                    exchange = @Exchange(MqConst.EXCHANGE_DIRECT_GOODS),
                    key = MqConst.ROUTING_GOODS_LOWER)
    })
    public void lowerGoods(Long skuId, Message message, Channel channel){
        try {
            System.out.println("下架商品的索引库："+skuId);
            listService.lowerGoods(skuId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
