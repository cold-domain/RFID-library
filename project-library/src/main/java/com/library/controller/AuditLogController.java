package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.constant.Constants;
import com.library.common.vo.Result;
import com.library.entity.AuditLog;
import com.library.service.AuditLogService;
import com.library.vo.AuditLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

/**
 * 审计日志控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询审计日志
     */
    @GetMapping
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

    /**
     * 查询审计日志详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        AuditLog log = auditLogService.getById(id);
        if (log == null) {
            return Result.error("日志记录不存在");
        }
        return Result.success(toVO(log));
    }

    /**
     * 清理过期日志
     */
    @PostMapping("/clean")
    public Result<?> clean(@RequestParam(required = false) Integer retentionDays) {
        int days = retentionDays != null ? retentionDays : Constants.LOG_RETENTION_DAYS;
        auditLogService.cleanExpiredLogs(days);
        return Result.success("过期日志清理完成");
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
        vo.setResultCode(log.getResultCode());
        vo.setExecutionTime(log.getExecutionTime());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setModuleName(log.getModuleName());
        if (log.getCreateTime() != null) {
            vo.setCreateTime(log.getCreateTime().format(FMT));
        }
        return vo;
    }
}
