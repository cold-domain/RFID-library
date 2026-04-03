package com.library.vo;

import lombok.Data;

@Data
public class ExceptionRecordVO {

    private Long id;
    private String exceptionType;
    private String exceptionLevel;
    private String exceptionContent;
    private String exceptionSource;
    private String exceptionDetails;
    private String username;
    private String ipAddress;
    private String requestUrl;
    private String requestMethod;
    private String requestSummary;
    private String stackTraceSummary;
    private Integer resolvedStatus;
    private String resolveNote;
    private String resolveTime;
    private String createTime;
}
