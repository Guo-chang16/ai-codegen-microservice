package com.guochang.aicodegenmicroservice.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.guochang.aicodegenmicroservice.model.entity.App;
import com.guochang.aicodegenmicroservice.model.entity.User;
import com.guochang.aicodegenmicroservice.service.AppService;
import com.guochang.aicodegenmicroservice.service.ProjectDownloadService;
import dev.langchain4j.agent.tool.P;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {


    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        //基础校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        File projectRoot = new File(projectPath);
        ThrowUtils.throwIf(!projectRoot.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在");
        ThrowUtils.throwIf(!projectRoot.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是目录");

        //设置HTTP响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));

        FileFilter fileFilter = file -> isPathAllowed(projectRoot.toPath(),file.toPath());
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8,false,fileFilter,projectRoot);
            log.info("打包下载项目成功！");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"打包下载项目失败");
        }
    }

    /**
     * 判断文件或目录是否需要忽略
     *
     * @param projectRoot 项目根目录
     * @param fullPath    文件或目录的完整路径
     * @return true表示需要忽略，false表示不需要忽略
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        Path relativizePath = projectRoot.relativize(fullPath);
        //检查路径是否符合要求
        for (Path path : relativizePath) {
            String name = path.toString();
            if (IGNORED_NAMES.contains(name)) {
                return false;
            }

            if (IGNORED_NAMES.stream().anyMatch(partName -> name.toLowerCase().endsWith(partName))) {
                return false;
            }
        }
        return true;
    }


}
