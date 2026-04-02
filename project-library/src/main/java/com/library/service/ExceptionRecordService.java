package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.ExceptionRecord;

/**
 * 异常记录服务接口
 */
public interface ExceptionRecordService extends IService<ExceptionRecord> {

    /**
     * 记录异常
     */
    void recordException(ExceptionRecord exceptionRecord);

    /**
     * 分页查询异常记录
     */
    IPage<ExceptionRecord> getExceptionPage(int pageNum, int pageSize, String exceptionType, String exceptionLevel, Integer resolvedStatus);

    /**
     * 处理异常
     */
    void resolveException(Long exceptionId, Long resolverId, String resolveNote);
}
