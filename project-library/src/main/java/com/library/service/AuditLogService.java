package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.AuditLog;
import com.library.vo.AuditLogOverviewVO;

public interface AuditLogService extends IService<AuditLog> {

    void recordLog(AuditLog auditLog);

    IPage<AuditLog> getAuditLogPage(int pageNum, int pageSize, String operationType, String logLevel, Long userId);

    void cleanExpiredLogs(int retentionDays);

    AuditLogOverviewVO getOverview();
}
