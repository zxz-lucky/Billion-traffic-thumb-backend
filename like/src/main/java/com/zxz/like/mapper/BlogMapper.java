package com.zxz.like.mapper;

import com.zxz.like.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2025-04-22 11:27:20
* @Entity generator.domain.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




