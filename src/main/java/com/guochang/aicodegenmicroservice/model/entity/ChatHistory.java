package com.guochang.aicodegenmicroservice.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 对话历史
 * @TableName chat_history
 */
@Data
@TableName(value ="chat_history")
@Builder
public class ChatHistory {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息
     */
    private String message;

    /**
     * user/ai
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID=1L;
}