package com.zxz.like.service;

import com.zxz.like.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zxz.like.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @description 针对表【blog】的数据库操作Service
* @createDate 2025-04-22 11:27:20
*/
public interface BlogService extends IService<Blog> {

    BlogVO getBlogVOById(long blogId, HttpServletRequest request);


    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);


}
