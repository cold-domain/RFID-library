package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.entity.ExceptionRecord;
import com.library.mapper.ExceptionRecordMapper;
import com.library.service.ExceptionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 异常记录服务实现类
 */
@Service
public class ExceptionRecordServiceImpl extends ServiceImpl<ExceptionRecordMapper, ExceptionRecord> implements ExceptionRecordService {

    @Override
    public void recordException(ExceptionRecord exceptionRecord) {
        if (exceptionRecord.getCreateTime() == null) {
            exceptionRecord.setCreateTime(LocalDateTime.now());
        }
        if (exceptionRecord.getResolvedStatus() == null) {
            exceptionRecord.setResolvedStatus(0);
        }
        baseMapper.insert(exceptionRecord);
    }

    @Override
    public IPage<ExceptionRecord> getExceptionPage(int pageNum, int pageSize, String exceptionType, String exceptionLevel, Integer resolvedStatus) {
        Page<ExceptionRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExceptionRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(exceptionType)) {
            wrapper.eq(ExceptionRecord::getExceptionType, exceptionType);
        }
        if (StringUtils.hasText(exceptionLevel)) {
            wrapper.eq(ExceptionRecord::getExceptionLevel, exceptionLevel);
        }
        if (resolvedStatus != null) {
            wrapper.eq(ExceptionRecord::getResolvedStatus, resolvedStatus);
        }
        wrapper.orderByDesc(ExceptionRecord::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void resolveException(Long exceptionId, Long resolverId, String resolveNote) {
        ExceptionRecord record = baseMapper.selectById(exceptionId);
        if (record == null) {
            throw new BusinessException("异常记录不存在");
        }
        ExceptionRecord update = new ExceptionRecord();
        update.setExceptionId(exceptionId);
        update.setResolvedStatus(1);
        update.setResolverId(resolverId);
        update.setResolveTime(LocalDateTime.now());
        update.setResolveNote(resolveNote);
        baseMapper.updateById(update);
    }
}
