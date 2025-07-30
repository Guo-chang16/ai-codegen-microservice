package com.guochang.aicodegenmicroservice.ai;

import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import com.guochang.aicodegenmicroservice.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceFactoryTest {
@Resource
private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void createAiCodeGeneratorService() {
        HtmlCodeResult res =aiCodeGeneratorService.generateHtmlCode("请生成一个html博客网站页面，不超过20行") ;
        Assertions.assertNotNull(res);
    }

    @Test
    void createAiCodeGeneratorService2() {
        MultiFileCodeResult res =aiCodeGeneratorService.generateMultiFileCode("请生成一个html博客网站页面，不超过50行") ;
        Assertions.assertNotNull(res);
    }
}