package com.guochang.aicodegenmicroservice.ai;

import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiCodeGenTypeRoutingService {



    /**
     * 生成标题
     * @param userMessage 用户消息
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum aiCodeGenTypeRouting(@UserMessage String userMessage);

}
