package com.guochang.aicodegenmicroservice.core;

import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;



    @Test
    void generateAndSaveCodeStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "生成一个打字网站", CodeGenTypeEnum.VUE_PROJECT, 4L);
        // 等待所有数据接收完毕
        List<String> res = stringFlux.collectList().block();
        // 将数据拼接成字符串
        String join = String.join("", res);
        Assertions.assertNotNull( join);
    }
}