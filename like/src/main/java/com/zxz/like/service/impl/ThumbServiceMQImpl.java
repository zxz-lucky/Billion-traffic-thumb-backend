package com.zxz.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxz.like.constant.RedisLuaScriptConstant;
import com.zxz.like.listener.thumb.msg.ThumbEvent;
import com.zxz.like.mapper.ThumbMapper;
import com.zxz.like.model.dto.thumb.DoThumbRequest;
import com.zxz.like.model.entity.Thumb;
import com.zxz.like.model.entity.User;
import com.zxz.like.model.enums.LuaStatusEnum;
import com.zxz.like.service.ThumbService;
import com.zxz.like.service.UserService;
import com.zxz.like.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//@Service("thumbService")
//@Slf4j
//@RequiredArgsConstructor
//public class ThumbServiceMQImpl extends ServiceImpl<ThumbMapper, Thumb>
//        implements ThumbService {
//
//    private final UserService userService;
//
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    private final PulsarTemplate<ThumbEvent> pulsarTemplate;
//
//    @Override
//    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
//        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
//            throw new RuntimeException("参数错误");
//        }
//        User loginUser = userService.getLoginUser(request);
//        Long loginUserId = loginUser.getId();
//        Long blogId = doThumbRequest.getBlogId();
//        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
//        // 执行 Lua 脚本，点赞存入 Redis
//        long result = redisTemplate.execute(
//                RedisLuaScriptConstant.THUMB_SCRIPT_MQ,
//                List.of(userThumbKey),
//                blogId
//        );
//        if (LuaStatusEnum.FAIL.getValue() == result) {
//            throw new RuntimeException("用户已点赞");
//        }
//
//        // 创建ThumbEvent对象，包含博客 ID、用户 ID、事件类型（点赞）和事件时间
//        ThumbEvent thumbEvent = ThumbEvent.builder()
//                .blogId(blogId)
//                .userId(loginUserId)
//                .type(ThumbEvent.EventType.INCR)
//                .eventTime(LocalDateTime.now())
//                .build();
//        // 使用 pulsarTemplate 将事件对象异步发送到thumb-topic主题
//        // 若发送失败，从 Redis 中删除该点赞记录，并记录错误日志
//        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
//            redisTemplate.opsForHash().delete(userThumbKey, blogId.toString(), true);
//            log.error("点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
//            return null;
//        });
//
//        return true;
//    }
//
//    @Override
//    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
//        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
//            throw new RuntimeException("参数错误");
//        }
//        User loginUser = userService.getLoginUser(request);
//        Long loginUserId = loginUser.getId();
//        Long blogId = doThumbRequest.getBlogId();
//        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
//        // 执行 Lua 脚本，点赞记录从 Redis 删除
//        long result = redisTemplate.execute(
//                RedisLuaScriptConstant.UNTHUMB_SCRIPT_MQ,
//                List.of(userThumbKey),
//                blogId
//        );
//        if (LuaStatusEnum.FAIL.getValue() == result) {
//            throw new RuntimeException("用户未点赞");
//        }
//
//        // 创建ThumbEvent对象，包含博客 ID、用户 ID、事件类型（取消点赞）和事件时间
//        ThumbEvent thumbEvent = ThumbEvent.builder()
//                .blogId(blogId)
//                .userId(loginUserId)
//                .type(ThumbEvent.EventType.DECR)
//                .eventTime(LocalDateTime.now())
//                .build();
//        // 使用pulsarTemplate将事件对象异步发送到thumb-topic主题
//        // 若发送失败，将点赞记录重新存入 Redis，并记录错误日志
//        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
//            redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), true);
//            log.error("点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
//            return null;
//        });
//
//        return true;
//    }
//
//    @Override
//    public Boolean hasThumb(Long blogId, Long userId) {
//        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
//    }
//
//}
