package com.zxz.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxz.like.constant.ThumbConstant;
import com.zxz.like.manager.cache.CacheManager;
import com.zxz.like.model.dto.thumb.DoThumbRequest;
import com.zxz.like.model.entity.Blog;
import com.zxz.like.model.entity.Thumb;
import com.zxz.like.mapper.ThumbMapper;
import com.zxz.like.model.entity.User;
import com.zxz.like.service.BlogService;
import com.zxz.like.service.ThumbService;
import com.zxz.like.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-22 11:27:20
 */

@Service("thumbService")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    /**
     * 点赞方法
     * @param doThumbRequest
     * @param request
     * @return
     */
    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {

        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);


        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();

//                boolean exists = this.lambdaQuery()
//                        .eq(Thumb::getUserId, loginUser.getId())
//                        .eq(Thumb::getBlogId, blogId)
//                        .exists();

                Boolean exists = this.hasThumb(blogId, loginUser.getId());

                if (exists) {
                    throw new RuntimeException("用户已点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);

                // 更新成功才执行
                //return update && this.save(thumb);

                boolean success = update && this.save(thumb);

                // 点赞记录存入 Redis
//                if (success) {
//                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString(), thumb.getId());
//                }

                if (success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    Long realThumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hashKey, fieldKey, realThumbId);
                    cacheManager.putIfPresent(hashKey, fieldKey, realThumbId);
                }

                // 更新成功才执行
                return success;


            });
        }
    }


    /**
     * 取消点赞方法
     * @param doThumbRequest
     * @param request
     * @return
     */
    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
//                Thumb thumb = this.lambdaQuery()
//                        .eq(Thumb::getUserId, loginUser.getId())
//                        .eq(Thumb::getBlogId, blogId)
//                        .one();
//                if (thumb == null) {
//                    throw new RuntimeException("用户未点赞");
//                }

                //判断是否点赞
//                Long thumbId = ((Long) redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString()));
//                if (thumbId == null) {
//                    throw new RuntimeException("用户未点赞");
//                }

                Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                if (thumbIdObj == null || thumbIdObj.equals(ThumbConstant.UN_THUMB_CONSTANT)) {
                    throw new RuntimeException("用户未点赞");
                }

                // 将 thumbIdObj 转换为 Long 类型
                Long thumbId = null;
                if (thumbIdObj instanceof Long) {
                    thumbId = (Long) thumbIdObj;
                } else if (thumbIdObj instanceof String) {
                    try {
                        thumbId = Long.parseLong((String) thumbIdObj);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("无法将获取到的点赞 ID 转换为 Long 类型", e);
                    }
                } else {
                    throw new RuntimeException("获取到的点赞 ID 类型不支持");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                //return update && this.removeById(thumb.getId());

                boolean success = update && this.removeById(thumbId);

                // 点赞记录从 Redis 删除
//                if (success) {
//                    redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
//                }

                // 点赞记录从 Redis 删除
                if (success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    redisTemplate.opsForHash().delete(hashKey, fieldKey);
                    cacheManager.putIfPresent(hashKey, fieldKey, ThumbConstant.UN_THUMB_CONSTANT);
                }



                return success;

            });
        }
    }


    @Resource
    private final RedisTemplate<String, Object> redisTemplate;

//    @Override
//    public Boolean hasThumb(Long blogId, Long userId) {
//        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
//    }


    // 引入缓存管理
    private final CacheManager cacheManager;

//    @Override
//    public Boolean hasThumb(Long blogId, Long userId) {
//        return cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString()) != null;
//    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = (Long) thumbIdObj;
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }



}




