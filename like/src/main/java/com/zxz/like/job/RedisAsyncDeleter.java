package com.zxz.like.job;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisAsyncDeleter {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisAsyncDeleter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void asyncDelete(String tempThumbKey) {
        // 创建并启动一个普通线程
        new Thread(() -> {
            redisTemplate.delete(tempThumbKey);
        }).start();
    }
}