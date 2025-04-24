package com.zxz.like.job;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;



@Component
public class AppShutdownHandler {

    @Resource
    private RedisAsyncDeleterWithThreadPool deleterWithThreadPool;

    @PreDestroy
    public void onShutdown() {
        deleterWithThreadPool.shutdown();
    }
}