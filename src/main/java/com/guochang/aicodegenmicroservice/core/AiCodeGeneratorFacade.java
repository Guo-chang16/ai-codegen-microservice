package com.guochang.aicodegenmicroservice.core;

import com.guochang.aicodegenmicroservice.ai.AiCodeGeneratorService;
import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import com.guochang.aicodegenmicroservice.ai.model.MultiFileCodeResult;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 门面模式：AiCodeGeneratorFacade
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 生成代码并保存
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        return switch (codeGenTypeEnum){
            case HTML-> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE-> generateAndSaveMultiFileCode(userMessage);
            default->{
                String  message = "不支持当前生成类型"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR,message);
            }
        };
    }

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        return switch (codeGenTypeEnum){
            case HTML-> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE-> generateAndSaveMultiFileCodeStream(userMessage);
            default->{
                String  message = "不支持当前生成类型"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR,message);
            }
        };
    }

    /**
     * 根据用户输入，生成html代码并保存
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //定义一个字符串拼接器
        StringBuilder codeflow = new StringBuilder();
        //添加一个doOnComplete方法，当流完成时执行
        return stringFlux.doOnNext(
        chunk->codeflow.append(chunk)
        ).doOnComplete(() -> {
            try {
                String flow = codeflow.toString();
                HtmlCodeResult result = CodeParser.parseHtmlCode(flow);
                File file = CodeFileSaver.saveHtmlCodeResult(result);
                log.info("保存成功！目录为{}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败:{}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 生成并保存多文件代码流
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //定义一个字符串拼接器
        StringBuilder codeflow = new StringBuilder();
        return stringFlux.doOnNext(
                chunk->codeflow.append(chunk)
        ).doOnComplete(() -> {
            try {
                String flow = codeflow.toString();
                MultiFileCodeResult result = CodeParser.parseMultiFileCode(flow);
                File file = CodeFileSaver.saveMultiFileCodeResult(result);
                log.info("保存成功！目录为{}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败:{}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 生成html代码并保存
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成多文件代码并保存
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }


}
