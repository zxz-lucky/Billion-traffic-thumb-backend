package com.zxz.like.controller;

import com.zxz.like.common.BaseResponse;
import com.zxz.like.common.ResultUtils;
import com.zxz.like.constant.UserConstant;
import com.zxz.like.model.entity.User;
import com.zxz.like.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {  
    @Resource
    private UserService userService;
  
    @GetMapping("/login")
    public BaseResponse<User> login(long userId, HttpServletRequest request) {
        User user = userService.getById(userId);  
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/login")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        return ResultUtils.success(loginUser);
    }

}
