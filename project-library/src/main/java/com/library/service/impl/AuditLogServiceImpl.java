package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.AuditLog;
import com.library.mapper.AuditLogMapper;
import com.library.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 审计日志服务实现类
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Override
    public void recordLog(AuditLog auditLog) {
        if (auditLog.getCreateTime() == null) {
            auditLog.setCreateTime(LocalDateTime.now());
        }
        baseMapper.insert(auditLog);
    }

    @Override
    public IPage<AuditLog> getAuditLogPage(int pageNum, int pageSize, String operationType, String logLevel, Long userId) {
        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(operationType)) {
            wrapper.eq(AuditLog::getOperationType, operationType);
        }
        if (StringUtils.hasText(logLevel)) {
            wrapper.eq(AuditLog::getLogLevel, logLevel);
        }
        if (userId != null) {
            wrapper.eq(AuditLog::getUserId, userId);
        }
        wrapper.orderByDesc(AuditLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void cleanExpiredLogs(int retentionDays) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(AuditLog::getCreateTime, LocalDateTime.now().minusDays(retentionDays));
        baseMapper.delete(wrapper);
    }
}
