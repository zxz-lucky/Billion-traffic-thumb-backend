package com.zxz.like.manager.cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 主要负责管理缓存和热点键检测
 */

@Component
@Slf4j
public class CacheManager {  
    private TopK hotKeyDetector;  //用于检测热点键。它会被初始化为 HeavyKeeper 类的实例.
    private Cache<String, Object> localCache;   //用于存储键值对。它会被初始化为 Caffeine 缓存的实例。
  
    @Bean
    public TopK getHotKeyDetector() {   //该方法创建并初始化了一个 HeavyKeeper 实例作为 hotKeyDetector
        hotKeyDetector = new HeavyKeeper(  
                // 监控 Top 100 Key  :表示要监控的前 100 个热点键
                100,  
                // 宽度  :数据结构的宽度，影响存储和计算的精度
                100000,  
                // 深度  :数据结构的深度，影响存储和计算的精度
                5,  
                // 衰减系数  :衰减系数，用于控制数据的老化速度，即随着时间推移，旧数据的权重会逐渐降低
                0.92,  
                // 最小出现 10 次才记录  :最小出现次数，只有某个键出现次数达到 10 次才会被记录和监控
                10  
        );  
        return hotKeyDetector;  
    }  
  
    @Bean  
    public Cache<String, Object> localCache() {  
        return localCache = Caffeine.newBuilder()
                .maximumSize(1000)  //设置缓存的最大容量为 1000 个键值对，当缓存中的元素数量超过这个值时，Caffeine 会根据其淘汰策略自动移除一些元素。
                .expireAfterWrite(5, TimeUnit.MINUTES)  //设置缓存项在写入后 5 分钟自动过期，过期后缓存项会被自动移除。
                .build();  
    }



    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 辅助方法：构造复合 key    复合键能确保在缓存中键的唯一性，避免不同业务场景下键的冲突。
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    /**
     * 该方法用于从缓存中获取数据，采用了先本地缓存后 Redis 缓存的多级缓存策略
     * @param hashKey
     * @param key
     * @return
     */
    public Object get(String hashKey, String key) {
        // 构造唯一的 composite key
        String compositeKey = buildCacheKey(hashKey, key);

        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
            // 记录访问次数（每次访问计数 +1）
            hotKeyDetector.add(key, 1); //记录该键的访问次数，然后返回该值
            return value;
        }

        // 2. 本地缓存未命中，查询 Redis
        //使用 redisTemplate.opsForHash().get 方法从 Redis 的哈希表中获取 hashKey 和 key 对应的值
        Object redisValue = redisTemplate.opsForHash().get(hashKey, key);
        if (redisValue == null) {
            return null;
        }

        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {     //若 AddResult 对象表明该键是热点键，且本地缓存中不存在该键，将 Redis 中的值存入本地缓存
            localCache.put(compositeKey, redisValue);
        }

        return redisValue;
    }


    /**
     * 若本地缓存中已存在指定的键，则更新该键对应的值
     * @param hashKey
     * @param key
     * @param value
     */
    public void putIfPresent(String hashKey, String key, Object value) {
        String compositeKey = buildCacheKey(hashKey, key);
        Object object = localCache.getIfPresent(compositeKey);
        if (object == null) {
            return;
        }
        localCache.put(compositeKey, value);
    }

    /**
     * 定时清理过期的热 Key 检测数据
     */
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS) //任务执行的固定时间间隔为 20
    public void cleanHotKeys() {
        hotKeyDetector.fading();
    }




}
