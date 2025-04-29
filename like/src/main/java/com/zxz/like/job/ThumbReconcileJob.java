package com.zxz.like.job;

import com.zxz.like.constant.ThumbConstant;
import com.zxz.like.listener.thumb.msg.ThumbEvent;
import com.zxz.like.service.ThumbService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 用于对 Redis 和 MySQL 中存储的点赞数据进行对账操作，并在数据不一致时发送补偿事件到 Pulsar 消息队列
 *  确保数据一致性
 */

//
//@Service
//@Slf4j
//public class ThumbReconcileJob {
//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Resource
//    private ThumbService thumbService;
//
//    @Resource
//    private PulsarTemplate<ThumbEvent> pulsarTemplate;
//
//    /**
//     * 定时任务入口（每天凌晨2点执行）
//     */
//    @Scheduled(cron = "0 0 2 * * ?")
//    public void run() {
//        long startTime = System.currentTimeMillis();
//
//        // 1. 获取该分片下的所有用户ID
//        Set<Long> userIds = new HashSet<>();
//        String pattern = ThumbConstant.USER_THUMB_KEY_PREFIX + "*";     //用于匹配 Redis 中所有以 ThumbConstant.USER_THUMB_KEY_PREFIX 开头的键。
//        try (Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
//            while (cursor.hasNext()) {      //使用 redisTemplate.scan 方法扫描 Redis 中的键，将匹配到的键中的用户 ID 提取出来并存储在 userIds 集合中。
//                String key = cursor.next();
//                Long userId = Long.valueOf(key.replace(ThumbConstant.USER_THUMB_KEY_PREFIX, ""));
//                userIds.add(userId);
//            }
//        }
//
//        // 2. 逐用户比对
//        userIds.forEach(userId -> {     //遍历 userIds 集合，对于每个用户 ID
//            //从 Redis 中获取该用户点赞的所有博客 ID，存储在 redisBlogIds 集合中
//            Set<Long> redisBlogIds = redisTemplate.opsForHash().keys(ThumbConstant.USER_THUMB_KEY_PREFIX + userId).stream().map(obj -> Long.valueOf(obj.toString())).collect(Collectors.toSet());
//            //从 MySQL 中获取该用户点赞的所有博客 ID，存储在 mysqlBlogIds 集合中
//            Set<Long> mysqlBlogIds = Optional.ofNullable(thumbService.lambdaQuery()
//                            .eq(Thumb::getUserId, userId)
//                            .list()
//                    ).orElse(new ArrayList<>())
//                    .stream()
//                    .map(Thumb::getBlogId)
//                    .collect(Collectors.toSet());
//
//            // 3. 计算差异（Redis有但MySQL无）
//            Set<Long> diffBlogIds = Sets.difference(redisBlogIds, mysqlBlogIds);
//
//            // 4. 发送补偿事件
//            sendCompensationEvents(userId, diffBlogIds);
//        });
//
//        log.info("对账任务完成，耗时 {}ms", System.currentTimeMillis() - startTime);
//    }
//
//    /**
//     * 发送补偿事件到Pulsar
//     */
//    private void sendCompensationEvents(Long userId, Set<Long> blogIds) {
//
//        //遍历 blogIds 集合，对于每个博客 ID，创建一个 ThumbEvent 对象，表示点赞事件
//        blogIds.forEach(blogId -> {
//            ThumbEvent thumbEvent = new ThumbEvent(userId, blogId, ThumbEvent.EventType.INCR, LocalDateTime.now());
//            //使用 pulsarTemplate.sendAsync 方法异步发送事件消息到 thumb-topic 主题
//            pulsarTemplate.sendAsync("thumb-topic", thumbEvent)
//                    .exceptionally(ex -> {  //如果发送过程中出现异常，使用 exceptionally 方法捕获异常并记录错误日志
//                        log.error("补偿事件发送失败: userId={}, blogId={}", userId, blogId, ex);
//                        return null;
//                    });
//        });
//    }
//}
