package com.guochang.aicodegenmicroservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.mapper.AppMapper;
import com.guochang.aicodegenmicroservice.model.dto.app.AppQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.App;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.model.vo.AppVO;
import com.guochang.aicodegenmicroservice.model.vo.UserVO;
import com.guochang.aicodegenmicroservice.service.AppService;
import com.guochang.aicodegenmicroservice.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author 31179
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-07-31 13:27:01
*/
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

    @Resource
    private UserService userService;


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }


@Override
public List<AppVO> getAppVOList(List<App> appList) {
    if (CollUtil.isEmpty(appList)) {
        return new ArrayList<>();
    }
    // 批量获取用户信息，避免 N+1 查询问题
    Set<Long> userIds = appList.stream()
            .map(App::getUserId)
            .collect(Collectors.toSet());
    Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, userService::getUserVO));
    return appList.stream().map(app -> {
        AppVO appVO = getAppVO(app);
        UserVO userVO = userVOMap.get(app.getUserId());
        appVO.setUser(userVO);
        return appVO;
    }).collect(Collectors.toList());
}


@Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotNull(initPrompt), "initPrompt", initPrompt);
        queryWrapper.eq(ObjUtil.isNotNull(priority), "priority", priority);
        queryWrapper.like(StrUtil.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StrUtil.isNotBlank(cover), "cover", cover);
        queryWrapper.like(StrUtil.isNotBlank(codeGenType), "codeGenType", codeGenType);
        queryWrapper.like(StrUtil.isNotBlank(deployKey), "deployKey", deployKey);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}




