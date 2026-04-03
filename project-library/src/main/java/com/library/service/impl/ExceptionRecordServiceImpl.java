package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.entity.ExceptionRecord;
import com.library.mapper.ExceptionRecordMapper;
import com.library.service.ExceptionRecordService;
import com.library.vo.ExceptionOverviewVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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
            throw new BusinessException("\u5f02\u5e38\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        ExceptionRecord update = new ExceptionRecord();
        update.setExceptionId(exceptionId);
        update.setResolvedStatus(1);
        update.setResolverId(resolverId);
        update.setResolveTime(LocalDateTime.now());
        update.setResolveNote(resolveNote);
        baseMapper.updateById(update);
    }

    @Override
    public void ignoreException(Long exceptionId, Long resolverId, String resolveNote) {
        ExceptionRecord record = baseMapper.selectById(exceptionId);
        if (record == null) {
            throw new BusinessException("\u5f02\u5e38\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        ExceptionRecord update = new ExceptionRecord();
        update.setExceptionId(exceptionId);
        update.setResolvedStatus(2);
        update.setResolverId(resolverId);
        update.setResolveTime(LocalDateTime.now());
        update.setResolveNote(resolveNote);
        baseMapper.updateById(update);
    }

    @Override
    public ExceptionOverviewVO getOverview() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        ExceptionOverviewVO overview = new ExceptionOverviewVO();
        overview.setTotalCount(count());
        overview.setTodayCount(count(new LambdaQueryWrapper<ExceptionRecord>().ge(ExceptionRecord::getCreateTime, todayStart)));
        overview.setPendingCount(count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getResolvedStatus, 0)));
        overview.setResolvedCount(count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getResolvedStatus, 1)));
        overview.setIgnoredCount(count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getResolvedStatus, 2)));
        overview.setHighRiskCount(count(new LambdaQueryWrapper<ExceptionRecord>().in(ExceptionRecord::getExceptionLevel, "high", "critical")));
        return overview;
    }
}
