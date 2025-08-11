package com.guochang.aicodegenmicroservice.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guochang.aicodegenmicroservice.annotation.AuthCheck;
import com.guochang.aicodegenmicroservice.common.BaseResponse;
import com.guochang.aicodegenmicroservice.common.DeleteRequest;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.common.Result;
import com.guochang.aicodegenmicroservice.constant.UserConstant;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.model.dto.user.*;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.model.vo.LoginUserVO;
import com.guochang.aicodegenmicroservice.model.vo.UserVO;
import com.guochang.aicodegenmicroservice.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return Result.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        //此时返回脱敏后的用户信息
        return Result.success(userService.getLoginUserVO(user));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return Result.success(result);
    }

    @PostMapping("/add")
    //最重要的：设置管理员权限
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null || userAddRequest.getUserAccount().length()< 4, ErrorCode.PARAMS_ERROR,"参数为空或账号名长度小于4");
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "新增用户失败");
        Long id = user.getId();
        return Result.success(id);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "请求的Id错误");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "没有该用户Id");
        //此时返回脱敏后的用户信息
        return Result.success(user);
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        //此时返回脱敏后的用户信息
        return Result.success(userService.getUserVO(user));
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserById(@RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(deleteRequest == null || id <= 0, ErrorCode.PARAMS_ERROR, "参数传递错误");
        boolean success = userService.removeById(id);
        return Result.success(success);
    }

    @PostMapping("/update")
    //最重要的：设置管理员权限
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean success = userService.updateById(user);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "新增用户失败");
        return Result.success(success);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return Result.success(userVOPage);
    }
}
