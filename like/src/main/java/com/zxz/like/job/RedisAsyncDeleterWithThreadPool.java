package com.zxz.like.job;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisAsyncDeleterWithThreadPool {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;

    public RedisAsyncDeleterWithThreadPool(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 创建一个单线程的线程池
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void asyncDelete(String tempThumbKey) {
        // 提交任务到线程池
        executorService.submit(() -> {
            redisTemplate.delete(tempThumbKey);
        });
    }

    // 在应用关闭时关闭线程池
    public void shutdown() {
        executorService.shutdown();
    }
}