package com.guochang.aicodegenmicroservice.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guochang.aicodegenmicroservice.annotation.AuthCheck;
import com.guochang.aicodegenmicroservice.common.BaseResponse;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.common.Result;
import com.guochang.aicodegenmicroservice.constant.UserConstant;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.ChatHistory;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.service.ChatHistoryService;
import com.guochang.aicodegenmicroservice.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;


    /**
     * 获取某个app的最新对话历史(指定app的特定展示条数)
     * @param appId          appId
     * @param pageSize       每页大小
     * @param lastCreateTime 最后创建时间(当前时间)
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") LocalDateTime lastCreateTime, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return Result.success(result);
    }

    /**
     * 获取所有app的对话历史
     * @return
     */
    @GetMapping("list")
    public List<ChatHistory> list(){return chatHistoryService.list();}

    /**
     * 根据对话id获取对话历史详情
     * @param id 对话历史id
     * @return 对话历史详情
     */
    @GetMapping("getInfo/{id}")
    public ChatHistory getInfo(@PathVariable Long id){
        return chatHistoryService.getById(id);
    }

    /**
     * 分页查询所有对话历史
     * @param page 分页参数
     * @return 对话历史分页
     */
    @GetMapping("page")
    public Page<ChatHistory> page(Page<ChatHistory> page){
        return chatHistoryService.page(page);
    }

    /**
     * 管理员分页查询所有对话历史
     * @param chatHistoryQueryRequest 查询请求
     *高级分页查询，支持多条件过滤和排序
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getCurrent();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return Result.success(result);
    }

}
