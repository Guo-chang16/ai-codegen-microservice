package com.guochang.aicodegenmicroservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guochang.aicodegenmicroservice.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.guochang.aicodegenmicroservice.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guochang.aicodegenmicroservice.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * @author 31179
 * @description 针对表【chat_history(对话历史)】的数据库操作Service
 * @createDate 2025-08-01 14:00:48
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);


    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
