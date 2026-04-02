package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 异常记录实体类
 */
@Data
@TableName("exceptions")
public class ExceptionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "exception_id", type = IdType.AUTO)
    private Long exceptionId;

    private String exceptionType;

    private String exceptionLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String exceptionSource;

    private String exceptionMessage;

    private String exceptionDetails;

    private Long userId;

    private String username;

    private String ipAddress;

    private String userAgent;

    private String requestUrl;

    private String requestMethod;

    private String requestSummary;

    private String stackTraceSummary;

    private Integer resolvedStatus;

    private Long resolverId;

    private LocalDateTime resolveTime;

    private String resolveNote;
}
