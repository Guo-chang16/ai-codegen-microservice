package com.guochang.aicodegenmicroservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.constant.AppConstant;
import com.guochang.aicodegenmicroservice.core.AiCodeGeneratorFacade;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.mapper.AppMapper;
import com.guochang.aicodegenmicroservice.model.dto.app.AppQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.App;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.model.enums.ChatHistoryMessageTypeEnum;
import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import com.guochang.aicodegenmicroservice.model.vo.AppVO;
import com.guochang.aicodegenmicroservice.model.vo.UserVO;
import com.guochang.aicodegenmicroservice.service.AppService;
import com.guochang.aicodegenmicroservice.service.ChatHistoryService;
import com.guochang.aicodegenmicroservice.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
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

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser) {
        //1.参数校验
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "请选择应用");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "请输入内容");

        //查询应用信息
        App app = this.getById(appId);

        //权限校验，仅本人可以与自己生成的应用对话
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限");

        //获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(codeGenType);

        //调用AI模型生成代码
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, enumByValue, appId);
        // 7. 收集AI响应内容并在完成后记录到对话历史
        StringBuilder aiResponseBuilder = new StringBuilder();
        return contentFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponse)) {
                        chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    }
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "请选择应用");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        //部署权限校验
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NO_AUTH_ERROR, "无操作权限");
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR, "无操作权限");

        //生成deployKey
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
            app.setDeployKey(deployKey);
        }

        //获取代码生成类型，获取原始代码生成路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        //检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码路径不存在");
        }

        //复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用部署失败: " + e.getMessage());
        }

        //更新数据库
        App newApp = new App();
        newApp.setId(appId);
        newApp.setDeployKey(deployKey);
        newApp.setDeployedTime(LocalDateTime.now());

        boolean update = this.updateById(app);
        ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"应用部署失败!");


        //返回可访问的URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

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

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }



}




