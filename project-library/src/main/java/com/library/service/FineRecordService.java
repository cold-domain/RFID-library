package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.FineRecord;
import com.library.vo.FineRecordVO;

import java.math.BigDecimal;

/**
 * 罚款记录服务接口
 */
public interface FineRecordService extends IService<FineRecord> {

    /**
     * 创建罚款记录
     */
    FineRecord createFine(Long userId, Long borrowRecordId, String fineType, BigDecimal amount, int overdueDays, Long operatorId);

    /**
     * 支付罚款
     */
    void payFine(Long fineId, BigDecimal amount, Long operatorId);

    /**
     * 减免罚款
     */
    void waiveFine(Long fineId, String reason, Long operatorId);

    /**
     * 分页查询罚款记录
     */
    IPage<FineRecordVO> getFinePage(int pageNum, int pageSize, Long userId, String fineStatus);

    /**
     * 查询用户未付罚款总额
     */
    BigDecimal getUnpaidAmount(Long userId);
}
