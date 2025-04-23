package com.zxz.like.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxz.like.constant.ThumbConstant;
import com.zxz.like.model.entity.Blog;
import com.zxz.like.mapper.BlogMapper;
import com.zxz.like.model.entity.Thumb;
import com.zxz.like.model.entity.User;
import com.zxz.like.model.vo.BlogVO;
import com.zxz.like.service.BlogService;
import com.zxz.like.service.ThumbService;
import com.zxz.like.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2025-04-22 11:27:20
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    public BlogServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }


    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);

        if (loginUser == null) {
            return blogVO;
        }

//        Thumb thumb = thumbService.lambdaQuery()
//                .eq(Thumb::getUserId, loginUser.getId())
//                .eq(Thumb::getBlogId, blog.getId())
//                .one();
//        blogVO.setHasThumb(thumb != null);

        Boolean exist = thumbService.hasThumb(blog.getId(), loginUser.getId());
        blogVO.setHasThumb(exist);


        return blogVO;
    }

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();

//        if (ObjUtil.isNotEmpty(loginUser)) {
//            Set<Long> blogIdSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
//            // 获取点赞
//            List<Thumb> thumbList = thumbService.lambdaQuery()
//                    .eq(Thumb::getUserId, loginUser.getId())
//                    .in(Thumb::getBlogId, blogIdSet)
//                    .list();
//
//            thumbList.forEach(blogThumb -> blogIdHasThumbMap.put(blogThumb.getBlogId(), true));
//        }


        if (ObjUtil.isNotEmpty(loginUser)) {
            List<Object> blogIdList = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            // 获取点赞
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogIdList);
            for (int i = 0; i < thumbList.size(); i++) {
                if (thumbList.get(i) == null) {
                    continue;
                }
                blogIdHasThumbMap.put(Long.valueOf(blogIdList.get(i).toString()), true);
            }
        }


        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return blogVO;
                })
                .toList();
    }


}




