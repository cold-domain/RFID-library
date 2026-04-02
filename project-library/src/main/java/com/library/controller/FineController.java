package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.FineRecord;
import com.library.service.FineRecordService;
import com.library.vo.FineRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 罚款管理控制器（馆员权限）
 */
@RestController
@RequestMapping("/librarian/fines")
public class FineController {

    @Autowired
    private FineRecordService fineRecordService;

    /**
     * 分页查询罚款记录
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String fineStatus) {
        IPage<FineRecordVO> page = fineRecordService.getFinePage(pageNum, pageSize, userId, fineStatus);
        return Result.success(page);
    }

    /**
     * 创建罚款
     */
    @PostMapping
    public Result<?> create(
            @RequestParam Long userId,
            @RequestParam(required = false) Long borrowRecordId,
            @RequestParam String fineType,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "0") int overdueDays) {
        Long operatorId = ContextHolder.getCurrentUserId();
        FineRecord fine = fineRecordService.createFine(userId, borrowRecordId, fineType, amount, overdueDays, operatorId);
        return Result.success("罚款创建成功", fine);
    }

    /**
     * 支付罚款
     */
    @PostMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id, @RequestParam BigDecimal amount) {
        Long operatorId = ContextHolder.getCurrentUserId();
        fineRecordService.payFine(id, amount, operatorId);
        return Result.success("缴费成功");
    }

    /**
     * 减免罚款
     */
    @PostMapping("/{id}/waive")
    public Result<?> waive(@PathVariable Long id, @RequestParam String reason) {
        Long operatorId = ContextHolder.getCurrentUserId();
        fineRecordService.waiveFine(id, reason, operatorId);
        return Result.success("罚款已减免");
    }

    /**
     * 查询用户未付罚款总额
     */
    @GetMapping("/unpaid/{userId}")
    public Result<?> unpaidAmount(@PathVariable Long userId) {
        BigDecimal amount = fineRecordService.getUnpaidAmount(userId);
        return Result.success(amount);
    }
}
