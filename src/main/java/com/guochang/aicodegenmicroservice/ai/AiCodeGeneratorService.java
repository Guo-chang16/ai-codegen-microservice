package com.guochang.aicodegenmicroservice.ai;

import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import com.guochang.aicodegenmicroservice.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 生成html代码
     * @param prompt 用户提示词
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String prompt);

    /**
     * 生成多文件代码
     * @param prompt 用户提示词
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String prompt);



    /**
     * 生成html代码
     * @param prompt 用户提示词
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String prompt);

    /**
     * 生成多文件代码
     * @param prompt 用户提示词
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String prompt);

    /**
     * 生成标题
     * @param userMessage 用户消息
     * @return
     */
    @SystemMessage(fromResource="prompt/title-system-prompt.txt")
    String generateTitle( @UserMessage String userMessage);

}
