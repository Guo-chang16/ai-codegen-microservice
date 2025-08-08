package com.guochang.aicodegenmicroservice.service;

import com.guochang.aicodegenmicroservice.model.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 下载项目压缩包
     * @return
     */
    void downloadProjectAsZip(String projectPath, String  downloadFileName, HttpServletResponse response);











}
