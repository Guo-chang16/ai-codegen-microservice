package com.guochang.aicodegenmicroservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guochang.aicodegenmicroservice.model.dto.app.AppAddRequest;
import com.guochang.aicodegenmicroservice.model.dto.app.AppQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.App;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
* @author 31179
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2025-07-31 13:27:01
*/
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser);

    String deployApp(Long appId, User loginUser);

    void generateAppScreenshotAsync(Long appId, String appUrl);

    long addApp(AppAddRequest appAddRequest,User loginUser);
}
