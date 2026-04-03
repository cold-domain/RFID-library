package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.constant.Constants;
import com.library.common.vo.Result;
import com.library.entity.AuditLog;
import com.library.service.AuditLogService;
import com.library.vo.AuditLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @PreAuthorize("hasAuthority('audit:view')")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) Long userId) {
        IPage<AuditLog> page = auditLogService.getAuditLogPage(pageNum, pageSize, operationType, logLevel, userId);
        IPage<AuditLogVO> voPage = page.convert(this::toVO);
        return Result.success(voPage);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('audit:view')")
    public Result<?> overview() {
        return Result.success(auditLogService.getOverview());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('audit:view')")
    public Result<?> getById(@PathVariable Long id) {
        AuditLog log = auditLogService.getById(id);
        if (log == null) {
            return Result.error("\u65e5\u5fd7\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        return Result.success(toVO(log));
    }

    @PostMapping("/clean")
    @PreAuthorize("hasAuthority('audit:clean')")
    public Result<?> clean(@RequestParam(required = false) Integer retentionDays) {
        int days = retentionDays != null ? retentionDays : Constants.LOG_RETENTION_DAYS;
        auditLogService.cleanExpiredLogs(days);
        return Result.success("\u8fc7\u671f\u65e5\u5fd7\u6e05\u7406\u5b8c\u6210");
    }

    private AuditLogVO toVO(AuditLog log) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(log.getAuditLogId());
        vo.setUsername(log.getUsername());
        vo.setOperationType(log.getOperationType());
        vo.setLogLevel(log.getLogLevel());
        vo.setOperationContent(log.getOperationDesc());
        vo.setIpAddress(log.getIpAddress());
        vo.setRequestMethod(log.getRequestMethod());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setRequestParams(log.getRequestParams());
        vo.setResponseData(log.getResponseData());
        vo.setResultCode(log.getResultCode());
        vo.setExecutionTime(log.getExecutionTime());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setModuleName(log.getModuleName());
        vo.setUserAgent(log.getUserAgent());
        if (log.getCreateTime() != null) {
            vo.setCreateTime(log.getCreateTime().format(FMT));
        }
        return vo;
    }
}
