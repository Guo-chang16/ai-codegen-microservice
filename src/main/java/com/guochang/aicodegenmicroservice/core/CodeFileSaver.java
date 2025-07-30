package com.guochang.aicodegenmicroservice.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import com.guochang.aicodegenmicroservice.ai.model.MultiFileCodeResult;
import com.guochang.aicodegenmicroservice.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器
 */
public class CodeFileSaver {
    //文件保存的根目录
    private static String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    //保存html页面
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }


    //保存多文件代码
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String dirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(dirPath, "index.html", result.getHtmlCode());
        writeToFile(dirPath, "style.css", result.getCssCode());
        writeToFile(dirPath, "script.js", result.getJsCode());
        return new File(dirPath);
    }

    //构建文件的唯一路径（tmp/code-output/saveType_雪花 ID）
    private static String buildUniqueDir(String saveType) {
        String uniqueDirName = StrUtil.format("{}_{}", saveType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }


    //保存单个文件
    public static void writeToFile(String dirPath, String fileName, String fileContent) {
        //将目录路径与文件名组合成完整文件路径
        String filePath = dirPath + File.separator + fileName;
        //写入文件
        File file = FileUtil.writeString(fileContent ,filePath, StandardCharsets.UTF_8);


    }

}
