package com.guochang.aicodegenmicroservice.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiCodeGenTitleService {

    /**
     * 生成标题
     * @param userMessage 用户消息
     * @return
     */
    @SystemMessage(fromResource="prompt/title-system-prompt.txt")
    String generateTitle( @UserMessage String userMessage);

}
