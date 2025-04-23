package com.zxz.like.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxz.like.mapper.BlogMapper;
import com.zxz.like.model.entity.Thumb;
import com.zxz.like.model.enums.ThumbTypeEnum;
import com.zxz.like.service.ThumbService;
import com.zxz.like.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时将 Redis 中的临时点赞数据同步到数据库  
 *  
 */  
@Component
@Slf4j
public class SyncThumb2DBJob {  
  
    @Resource
    private ThumbService thumbService;
  
    @Resource  
    private BlogMapper blogMapper;
  
    @Resource  
    private RedisTemplate<String, Object> redisTemplate;
  
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {  
        log.info("开始执行");  
        DateTime nowDate = DateUtil.date();
        // 如果秒数为0~9 则回到上一分钟的50秒
        int second = (DateUtil.second(nowDate) / 10 - 1) * 10;
        if (second == -10) {
            second = 50;
            // 回到上一分钟
            nowDate = DateUtil.offsetMinute(nowDate, -1);
        }
        String date = DateUtil.format(nowDate, "HH:mm:") + second;
        syncThumb2DBByDate(date);
        log.info("临时数据同步完成");
    }  
  
    public void syncThumb2DBByDate(String date) {  
        // 获取到临时点赞和取消点赞数据  
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = CollUtil.isEmpty(allTempThumbMap);
  
        // 同步 点赞 到数据库  
        // 构建插入列表并收集blogId  
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        if (thumbMapEmpty) {  
            return;  
        }  
        ArrayList<Thumb> thumbList = new ArrayList<>();
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;  
        for (Object userIdBlogIdObj : allTempThumbMap.keySet()) {  
            String userIdBlogId = (String) userIdBlogIdObj;  
            String[] userIdAndBlogId = userIdBlogId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndBlogId[0]);  
            Long blogId = Long.valueOf(userIdAndBlogId[1]);  
            // -1 取消点赞，1 点赞  
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdBlogId).toString());  
            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                Thumb thumb = new Thumb();  
                thumb.setUserId(userId);  
                thumb.setBlogId(blogId);  
                thumbList.add(thumb);  
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {  
                // 拼接查询条件，批量删除  
                needRemove = true;  
                wrapper.or().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId);  
            } else {  
                if (thumbType != ThumbTypeEnum.NON.getValue()) {  
                    log.warn("数据异常：{}", userId + "," + blogId + "," + thumbType);  
                }  
                continue;  
            }  
            // 计算点赞增量  
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + thumbType);  
        }  
        // 批量插入  
        thumbService.saveBatch(thumbList);  
        // 批量删除  
        if (needRemove) {  
            thumbService.remove(wrapper);  
        }  
        // 批量更新博客点赞量  
        if (!blogThumbCountMap.isEmpty()) {  
            blogMapper.batchUpdateThumbCount(blogThumbCountMap);  
        }

        // 异步删除

//        Thread.startVirtualThread(() -> {
//            redisTemplate.delete(tempThumbKey);
//        });


//        // 使用普通线程
//        RedisAsyncDeleter deleter = new RedisAsyncDeleter(redisTemplate);
//        deleter.asyncDelete("tempThumbKey");

        // 使用线程池
        RedisAsyncDeleterWithThreadPool deleterWithThreadPool = new RedisAsyncDeleterWithThreadPool(redisTemplate);
        deleterWithThreadPool.asyncDelete("tempThumbKey");
        // 关闭线程池
        deleterWithThreadPool.shutdown();



    }  
}
