package com.guochang.aicodegenmicroservice.core.saver;

import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import com.guochang.aicodegenmicroservice.ai.model.MultiFileCodeResult;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 * @author yupi
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeSaverTemplateImpl htmlCodeFileSaver = new HtmlCodeSaverTemplateImpl();

    private static final MultiFileCodeSaverTemplateImpl multiFileCodeFileSaver = new MultiFileCodeSaverTemplateImpl();

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
