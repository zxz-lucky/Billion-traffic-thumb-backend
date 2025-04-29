package com.zxz.like.listener.thumb.msg;

import ch.qos.logback.core.joran.sanity.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxz.like.mapper.BlogMapper;
import com.zxz.like.model.entity.Thumb;
import com.zxz.like.service.ThumbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 *     用于消费 Pulsar 消息队列中的点赞事件消息，并对数据库进行相应的更新操作。
 */

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ThumbConsumer {
//
//    private final BlogMapper blogMapper;
//    private final ThumbService thumbService;
//
//    // 批量处理配置
//    @PulsarListener(
//            subscriptionName = "thumb-subscription",    //指定订阅名称为 thumb-subscription
//            topics = "thumb-topic",         //指定要监听的主题为 thumb-topic
//            schemaType = SchemaType.JSON,   //指定消息的序列化类型为 JSON
//            batch = true,                   //设置为 true，表示启用批量消费
//            consumerCustomizer = "thumbConsumerConfig"  //指定消费者的自定义配置类为 thumbConsumerConfig
//            // 引用 NACK 重试策略
//            negativeAckRedeliveryBackoff = "negativeAckRedeliveryBackoff",
//            // 引用 ACK 超时重试策略
//            ackTimeoutRedeliveryBackoff = "ackTimeoutRedeliveryBackoff"
//            // 死信仅适用于 shared 类型
//            subscriptionType = SubscriptionType.Shared,
//            // 引用死信队列策略
//            deadLetterPolicy = "deadLetterPolicy"
//    )
//    @Transactional(rollbackFor = Exception.class)   //Spring 的事务注解，确保该方法内的数据库操作在一个事务中执行，若出现异常则回滚事务
//    public void processBatch(List<ReactiveSubscription.Message<ThumbEvent>> messages) {
//
//        log.info("ThumbConsumer processBatch: {}", messages.size());
//        Map<Long, Long> countMap = new ConcurrentHashMap<>();   //用于记录每个博客的点赞数变化
//        List<Thumb> thumbs = new ArrayList<>(); //用于存储需要插入到数据库的点赞记录
//
//        // 并行处理消息
//        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
//        AtomicReference<Boolean> needRemove = new AtomicReference<>(false); //用于标记是否需要删除点赞记录
//
//        // 提取事件并过滤无效消息
//        List<ThumbEvent> events = messages.stream()
//                .map(Message::getValue)
//                .filter(Objects::nonNull)
//                .toList();
//
//
//        // 按(userId, blogId)分组，并获取每个分组的最新事件
//        Map<Pair<Long, Long>, ThumbEvent> latestEvents = events.stream()
//                .collect(Collectors.groupingBy(
//                        e -> Pair.of(e.getUserId(), e.getBlogId()),
//                        Collectors.collectingAndThen(
//                                Collectors.toList(),
//                                list -> {
//                                    // 按时间升序排序，取最后一个作为最新事件
//                                    list.sort(Comparator.comparing(ThumbEvent::getEventTime));
//                                    if (list.size() % 2 == 0) {  //如果分组内事件数量为偶数，则认为该分组的点赞和取消点赞操作相互抵消，忽略该分组
//                                        return null;
//                                    }
//                                    return list.get(list.size() - 1);
//                                }
//                        )
//                ));
//
//        //遍历最新事件，根据事件类型进行不同处理
//        latestEvents.forEach((userBlogPair, event) -> {
//            if (event == null) {
//                return;
//            }
//            ThumbEvent.EventType finalAction = event.getType();
//
//            //如果是点赞事件（EventType.INCR），则将对应博客的点赞数加 1，并创建一个 Thumb 对象添加到 thumbs 列表中
//            if (finalAction == ThumbEvent.EventType.INCR) {
//                countMap.merge(event.getBlogId(), 1L, Long::sum);
//                Thumb thumb = new Thumb();
//                thumb.setBlogId(event.getBlogId());
//                thumb.setUserId(event.getUserId());
//                thumbs.add(thumb);
//            } else {    //如果是取消点赞事件（EventType.DECR），则标记需要删除点赞记录，并将对应博客的点赞数减 1
//                needRemove.set(true);
//                wrapper.or().eq(Thumb::getUserId, event.getUserId()).eq(Thumb::getBlogId, event.getBlogId());
//                countMap.merge(event.getBlogId(), -1L, Long::sum);
//            }
//        });
//
//        // 如果需要删除点赞记录，则调用 thumbService.remove 方法删除符合条件的点赞记录
//        if (needRemove.get()) {
//            thumbService.remove(wrapper);
//        }
//        batchUpdateBlogs(countMap);
//        batchInsertThumbs(thumbs);
//    }
//
//    //用于批量更新博客的点赞数
//    public void batchUpdateBlogs(Map<Long, Long> countMap) {
//        if (!countMap.isEmpty()) {
//            blogMapper.batchUpdateThumbCount(countMap);
//        }
//    }
//
//    //用于批量插入点赞记录
//    public void batchInsertThumbs(List<Thumb> thumbs) {
//        if (!thumbs.isEmpty()) {
//            // 分批次插入
//            thumbService.saveBatch(thumbs, 500);
//        }
//    }


//@PulsarListener(topics = "thumb-dlq-topic")
//public void consumerDlq(Message<ThumbEvent> message) {
//    MessageId messageId = message.getMessageId();
//    log.info("dlq message = {}", messageId);
//    log.info("消息 {} 已入库", messageId);
//    log.info("已通知相关人员 {} 处理消息 {}", "坤哥", messageId);
//}



//}
