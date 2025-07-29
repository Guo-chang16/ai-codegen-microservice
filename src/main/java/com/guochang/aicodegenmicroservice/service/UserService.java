package com.guochang.aicodegenmicroservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guochang.aicodegenmicroservice.model.dto.user.UserQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.model.vo.LoginUserVO;
import com.guochang.aicodegenmicroservice.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 31179
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-29 16:01:44
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 获取加密后的密码
     *
     * @param userPassword 原始密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request httpRequest 请求方便设置 cookie
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request request
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 获取脱敏的已登录用户信息
     * @return 脱敏的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户退出登录
     *
     * @param request request
     * @return  退出结果
     */
    boolean userLogout(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    //获取mp查询所需的方法
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

}
