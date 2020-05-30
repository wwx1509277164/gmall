package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //指定 ConfirmCallback
        rabbitTemplate.setReturnCallback(this);             //指定 ReturnCallback
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送成功：" + JSON.toJSONString(correlationData));
        } else {
            log.info("消息发送失败：" + cause + " 数据：" + JSON.toJSONString(correlationData));
            //发送失败后进行从新发送
            //需要知道交换机，routingKey和消息体
            GmallCorrelationData gmallCorrelationData = (GmallCorrelationData)correlationData;
            this.reTrySendMessage(gmallCorrelationData);
        }
    }

    //从新发送信息
    public void reTrySendMessage(GmallCorrelationData gmallCorrelationData){
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount<2){
            //追加次数，因为不能一直进行发送
            gmallCorrelationData.setRetryCount(++retryCount);
            log.info("重新发送:"+JSON.toJSONString(gmallCorrelationData));
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),
                                          gmallCorrelationData.getRoutingKey(),
                                          gmallCorrelationData.getMessage(),gmallCorrelationData);
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        // 反序列化对象输出
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);
    }

 }
