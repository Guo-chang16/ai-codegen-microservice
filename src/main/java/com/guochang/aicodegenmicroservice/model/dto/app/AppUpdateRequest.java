package com.guochang.aicodegenmicroservice.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用更新请求
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;


    private static final long serialVersionUID = 1L;
} 