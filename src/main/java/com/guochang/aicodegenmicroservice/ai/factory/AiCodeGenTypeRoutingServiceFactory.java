package com.guochang.aicodegenmicroservice.ai.factory;


import com.guochang.aicodegenmicroservice.ai.service.AiCodeGenTypeRoutingService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {

    @Resource
    private ChatModel chatModel;


    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

}
