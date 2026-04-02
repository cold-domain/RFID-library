package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.AuditLog;

/**
 * 审计日志服务接口
 */
public interface AuditLogService extends IService<AuditLog> {

    /**
     * 记录审计日志
     */
    void recordLog(AuditLog auditLog);

    /**
     * 分页查询审计日志
     */
    IPage<AuditLog> getAuditLogPage(int pageNum, int pageSize, String operationType, String logLevel, Long userId);

    /**
     * 清理过期日志
     */
    void cleanExpiredLogs(int retentionDays);
}
