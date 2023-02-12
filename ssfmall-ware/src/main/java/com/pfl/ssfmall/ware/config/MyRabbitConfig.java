package com.pfl.ssfmall.ware.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;

@Configuration
public class MyRabbitConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;


    @RabbitListener
    public void Handler(Message message) {

    }
    /**
     * 使用 JSON 序列化机制，进行消息转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();

    }

    @Bean
    public Exchange stockEventExchange() {
        /**
         * String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
         */
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        HashMap<String, Object> argument = new HashMap<>();
        argument.put("x-dead-letter-exchange", "stock-event-exchange");
        argument.put("x-dead-letter-routing-key", "stock.release");
        argument.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, argument);
    }


    @Bean
    public Binding stockReleasedBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }
//    @PostConstruct // 在 MyRabbitConfig 对象创建完成之后，执行这个方法
//    public void initRabbitTemplate() {
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            /**
//             *
//             * @param correlationData 当前消息的唯一关联数据 （这个消息的唯一 id）
//             * @param b 消息是否收到
//             * @param s 失败的原因
//             */
//            @Override
//            public void confirm(CorrelationData correlationData, boolean b, String s) {
//                System.out.println("confirm....correlationData ====> [" + correlationData + "]" + "ack ===> [" + b + "]" +
//                        "cause =====> " + s);
//            }
//        });
//    }
}
