package com.guochang.aicodegenmicroservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guochang.aicodegenmicroservice.ai.service.AiCodeGenTitleService;
import com.guochang.aicodegenmicroservice.ai.service.AiCodeGenTypeRoutingService;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.constant.AppConstant;
import com.guochang.aicodegenmicroservice.core.AiCodeGeneratorFacade;
import com.guochang.aicodegenmicroservice.core.builder.VueProjectBuilder;
import com.guochang.aicodegenmicroservice.core.handler.StreamHandlerExecutor;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.mapper.AppMapper;
import com.guochang.aicodegenmicroservice.model.dto.app.AppAddRequest;
import com.guochang.aicodegenmicroservice.model.dto.app.AppQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.App;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.model.enums.ChatHistoryMessageTypeEnum;
import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import com.guochang.aicodegenmicroservice.model.vo.AppVO;
import com.guochang.aicodegenmicroservice.model.vo.UserVO;
import com.guochang.aicodegenmicroservice.service.AppService;
import com.guochang.aicodegenmicroservice.service.ChatHistoryService;
import com.guochang.aicodegenmicroservice.service.ScreenshotService;
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

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTitleService aiCodeGenTitleService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    @Resource
    private AppService appService;

    /**
     * 添加应用
     * @param appAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addApp(AppAddRequest appAddRequest, User loginUser) {
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        String initPrompt = appAddRequest.getInitPrompt();
        String title = aiCodeGenTitleService.generateTitle(initPrompt);
        //app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        app.setAppName(title);
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.aiCodeGenTypeRouting(initPrompt);
        app.setCodeGenType(codeGenTypeEnum.getValue());
        // 插入数据库
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }



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
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);

        // 5. 在调用 AI 前，先保存用户消息到数据库中
        chatHistoryService.addChatMessage(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());

        //调用AI模型生成代码
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        //收集AI响应内容并在完成后记录到对话历史
        return streamHandlerExecutor.doExecute(contentFlux,chatHistoryService,appId,loginUser,codeGenTypeEnum);
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

        //检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        //Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
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
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);

        //异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);

        return appDeployUrl;

    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
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




