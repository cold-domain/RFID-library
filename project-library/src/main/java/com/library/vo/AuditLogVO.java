package com.library.vo;

import lombok.Data;

/**
 * 审计日志视图对象（字段名匹配前端）
 */
@Data
public class AuditLogVO {

    private Long id;

    private String username;

    private String operationType;

    private String logLevel;

    /** 操作内容（对应Entity的operationDesc） */
    private String operationContent;

    private String ipAddress;

    private String requestMethod;

    private String requestUrl;

    private Integer resultCode;

    private Long executionTime;

    private String errorMessage;

    private String moduleName;

    private String createTime;
}
