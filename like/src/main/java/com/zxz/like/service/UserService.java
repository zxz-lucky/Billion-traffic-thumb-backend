package com.zxz.like.service;

import com.zxz.like.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-22 11:27:21
*/
public interface UserService extends IService<User> {


    User getLoginUser(HttpServletRequest request);

}
