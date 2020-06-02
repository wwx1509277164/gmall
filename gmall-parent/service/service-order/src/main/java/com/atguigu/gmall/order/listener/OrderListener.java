package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-31 22:46
 */
@Component
public class OrderListener {
    @Autowired
    OrderInfoService orderInfoService;
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel){
        try {
            System.out.println("订单Id："+orderId+"时间:"+new Date());
            orderInfoService.cancelOrder(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paySuccess(Long orderId, Message message, Channel channel){
        try {
            System.out.println("订单Id:"+orderId+"时间:"+new Date());
            //更新订单状态
            orderInfoService.updateOrderStatus(orderId, OrderStatus.FINISHED,
                    ProcessStatus.PAID);

            //发送消息通知仓库进行扣减库存操作
            orderInfoService.sendOrderStatus(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //接收仓库发送的消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = MqConst.ROUTING_WARE_ORDER
    ))
    public void updateWareOrder(String msg, Message message, Channel channel){
        try {
            System.out.println("扣减库存之后通知：" + msg);
            Map map = JSONObject.parseObject(msg, Map.class);
            if("DEDUCTED".equals(map.get("status"))){
                //扣减成功   更新  订单状态  进度状态
                orderInfoService.updateOrderStatus(Long.parseLong((String)map.get("orderId")),
                        OrderStatus.WAITING_DELEVER,
                        ProcessStatus.WAITING_DELEVER);
            }else{
                //1:通知   ： 补货
                //2：扣减失败  更新 进度状态
                orderInfoService.updateOrderStatus(Long.parseLong((String)map.get("orderId"))
                        ,ProcessStatus.STOCK_EXCEPTION);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
