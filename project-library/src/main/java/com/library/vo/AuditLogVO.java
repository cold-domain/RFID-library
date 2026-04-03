package com.library.vo;

import lombok.Data;

@Data
public class AuditLogVO {

    private Long id;
    private String username;
    private String operationType;
    private String logLevel;
    private String operationContent;
    private String ipAddress;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseData;
    private Integer resultCode;
    private Long executionTime;
    private String errorMessage;
    private String moduleName;
    private String userAgent;
    private String createTime;
}
