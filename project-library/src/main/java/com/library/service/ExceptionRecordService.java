package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.ExceptionRecord;
import com.library.vo.ExceptionOverviewVO;

public interface ExceptionRecordService extends IService<ExceptionRecord> {

    void recordException(ExceptionRecord exceptionRecord);

    IPage<ExceptionRecord> getExceptionPage(int pageNum, int pageSize, String exceptionType, String exceptionLevel, Integer resolvedStatus);

    void resolveException(Long exceptionId, Long resolverId, String resolveNote);

    void ignoreException(Long exceptionId, Long resolverId, String resolveNote);

    ExceptionOverviewVO getOverview();
}
