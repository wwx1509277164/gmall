package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @author Administrator
 * @create 2020-05-29 19:03
 */
@Component
public class MessageReceiver {


    //接收延迟消息
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void deadLeaderMessage(String msg,Channel channel,Message message){
        System.out.println("接收延迟消息："+msg+":"+new Date());
    }


    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void delayedMessage(String msg,Channel channel,Message message){
        System.out.println("接收延迟消息："+msg+":"+new Date());
    }











    //接收方
    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(value = "queue11",autoDelete = "false",durable = "true"),
            exchange = @Exchange(value = "exchange11"),
            key = {"routingKey11"}
    )})
    public void receiverMessage(String msg, Channel channel, Message message){
        //参数1  标记
        //参数2  Queue将消息删除吧
        try {
            //int i = 1/0;
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            if (message.getMessageProperties().isRedelivered()){
                //第二次失败之后
                try {
                    System.out.println("拒绝再次消费");
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }else {
                //给一次机会
                try {
                    System.out.println("给你一次机会");
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(msg);
    }
}
