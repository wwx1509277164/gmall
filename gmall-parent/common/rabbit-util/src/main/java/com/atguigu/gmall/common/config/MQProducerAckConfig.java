package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

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
            String jsonString = JSONObject.toJSONString(gmallCorrelationData);
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(),jsonString,1, TimeUnit.MINUTES);
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),
                                          gmallCorrelationData.getRoutingKey(),
                                          gmallCorrelationData.getMessage(),gmallCorrelationData);
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);
        //重新发送  次数做不到追加
        //Message message   相当于Request对象
        //Request对象  1：请求头  2：请求体
        //Message message  1：消息头  2：消息体
        //       消息头中有主键   发送消息的时候  第四个参数对象
        String uuid = message.getMessageProperties()
                .getHeader("spring_returned_message_correlation");
        if(StringUtils.isEmpty(uuid)){
            log.error("获取不到UUId无法进行消息重新发送");
            return;
        }
        String gmallJson = (String) redisTemplate
                .opsForValue().get(uuid);

        if(StringUtils.isEmpty(gmallJson)){
            log.error("获取不到GmallCorrelationData无法完成消息的发送");
            return;
        }
        GmallCorrelationData gmallCorrelationData = JSONObject.
                parseObject(gmallJson, GmallCorrelationData.class);
        //判断是不是延迟
        if(gmallCorrelationData.isDelay()){
            log.error("本次队列失败应答是正常的、不必重新发送消息");
            return;
        }

        System.out.println("队列应答失败：返回来的UUID：" + uuid);
        //可以重新发送消息
        this.reTrySendMessage(gmallCorrelationData);
    }

 }
