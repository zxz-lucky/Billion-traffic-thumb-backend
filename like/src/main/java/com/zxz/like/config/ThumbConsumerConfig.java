package com.zxz.like.config;

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
//}
