package com.guochang.aicodegenmicroservice.ai;

import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import com.guochang.aicodegenmicroservice.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 生成html代码
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);



    /**
     * 生成html代码
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成多文件代码
     * @return
     */
    @SystemMessage(fromResource="prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);




}
