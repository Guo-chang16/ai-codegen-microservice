package com.guochang.aicodegenmicroservice.core;

import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        aiCodeGeneratorFacade.generateAndSaveCode("生成一个html页面，页面为登陆页面", CodeGenTypeEnum.HTML);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个页面，页面为登陆页面", CodeGenTypeEnum.HTML);
        // 等待所有数据接收完毕
        List<String> res = stringFlux.collectList().block();
        // 将数据拼接成字符串
        String join = String.join("", res);
        Assertions.assertNotNull( join);
    }


}