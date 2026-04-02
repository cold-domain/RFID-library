package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体类
 */
@Data
@TableName("audit_logs")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "audit_log_id", type = IdType.AUTO)
    private Long auditLogId;

    private String operationType;

    private String operationDesc;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer resultCode;

    private Long userId;

    private String username;

    private String logLevel;

    private String ipAddress;

    private String userAgent;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String responseData;

    private Long executionTime;

    private String errorMessage;

    private String moduleName;

    private String remark;
}
