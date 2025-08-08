package com.guochang.aicodegenmicroservice.ai;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.guochang.aicodegenmicroservice.ai.tools.FileWriteTool;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import com.guochang.aicodegenmicroservice.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGenTitleServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGenTitleService aiCodeGenTitleService(){
        return AiServices.builder(AiCodeGenTitleService.class)
                .chatModel(chatModel)
                .build();
    }
}
