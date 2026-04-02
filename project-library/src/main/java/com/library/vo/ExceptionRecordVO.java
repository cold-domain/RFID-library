package com.library.vo;

import lombok.Data;

/**
 * 异常记录视图对象（字段名匹配前端）
 */
@Data
public class ExceptionRecordVO {

    private Long id;

    private String exceptionType;

    private String exceptionLevel;

    /** 异常内容（对应Entity的exceptionMessage） */
    private String exceptionContent;

    private String exceptionSource;

    private String username;

    private String ipAddress;

    private String requestUrl;

    private String requestMethod;

    private Integer resolvedStatus;

    private String resolveNote;

    private String resolveTime;

    private String createTime;
}
