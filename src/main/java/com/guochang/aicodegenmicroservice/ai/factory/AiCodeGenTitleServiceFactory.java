package com.guochang.aicodegenmicroservice.ai.factory;


import com.guochang.aicodegenmicroservice.ai.service.AiCodeGenTitleService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class AiCodeGenTitleServiceFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 创建AiCodeGenTitleService实例
     * @return
     */
    @Bean
    public AiCodeGenTitleService aiCodeGenTitleService(){
        return AiServices.builder(AiCodeGenTitleService.class)
                .chatModel(chatModel)
                .build();
    }
}
