package com.zxz.like.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 批量处理策略配置  
 */


//@Configuration
//public class ThumbConsumerConfig<T> implements PulsarListenerConsumerBuilderCustomizer<T> {
//    @Override
//    public void customize(ConsumerBuilder<T> consumerBuilder) {
//        consumerBuilder.batchReceivePolicy(
//                BatchReceivePolicy.builder()
//                        // 每次处理 1000 条
//                        .maxNumMessages(1000)
//                        // 设置超时时间（单位：毫秒）
//                        .timeout(10000, TimeUnit.MILLISECONDS)
//                        .build()
//        );
//    }
//
//    // 配置 NACK 重试策略
//    @Bean
//    public RedeliveryBackoff negativeAckRedeliveryBackoff() { //用于定义 NACK（否定确认）的重试策略。当消费者处理消息失败并发送 NACK 时，Pulsar 会根据这个策略对消息进行重试
//        return MultiplierRedeliveryBackoff.builder()  //创建一个重试策略实例
//                // 初始延迟 1 秒
//                .minDelayMs(1000)     //第一次重试会在消息被 NACK 后的 1 秒后进行
//                // 最大延迟 60 秒
//                .maxDelayMs(60_000)   //无论重试次数多少，重试间隔不会超过这个最大值
//                // 每次重试延迟倍数
//                .multiplier(2)        //设置每次重试的延迟倍数为 2。也就是说，每次重试的延迟时间是上一次的 2 倍。例如，第一次重试延迟 1 秒，第二次重试延迟 2 秒，第三次重试延迟 4 秒，以此类推，直到达到最大延迟时间
//                .build();
//    }
//
//    // 配置 ACK 超时重试策略
//    @Bean
//    public RedeliveryBackoff ackTimeoutRedeliveryBackoff() {  //当消费者在规定的 ACK 超时时间内没有发送 ACK 时，Pulsar 会根据这个策略对消息进行重试
//        return MultiplierRedeliveryBackoff.builder()
//                // 初始延迟 5 秒
//                .minDelayMs(5000)
//                // 最大延迟 300 秒
//                .maxDelayMs(300_000)
//                .multiplier(3)
//                .build();
//    }
//
//
//    @Bean
//    public DeadLetterPolicy deadLetterPolicy() {
//        return DeadLetterPolicy.builder()
//                // 最大重试次数
//                .maxRedeliverCount(3)
//                // 死信主题名称
//                .deadLetterTopic("thumb-dlq-topic")
//                .build();
//    }
//
//
//
//}
