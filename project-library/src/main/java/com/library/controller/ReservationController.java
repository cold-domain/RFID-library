package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.service.BookReservationService;
import com.library.vo.ReservationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 预约管理控制器（馆员权限）
 */
@RestController
@RequestMapping("/librarian/reservations")
public class ReservationController {

    @Autowired
    private BookReservationService bookReservationService;

    /**
     * 分页查询所有预约记录
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        // 反向映射：前端 completed → 后端 picked_up
        if ("completed".equals(status)) {
            status = "picked_up";
        }
        IPage<ReservationVO> page = bookReservationService.getReservationPage(pageNum, pageSize, userId, status);
        return Result.success(page);
    }

    /**
     * 确认取书
     */
    @PostMapping("/{id}/pickup")
    public Result<?> confirmPickup(@PathVariable Long id) {
        Long operatorId = ContextHolder.getCurrentUserId();
        bookReservationService.confirmPickup(id, operatorId);
        return Result.success("取书确认成功");
    }

    /**
     * 手动处理过期预约
     */
    @PostMapping("/check-expired")
    public Result<?> checkExpired() {
        bookReservationService.checkExpiredReservations();
        return Result.success("过期预约处理完成");
    }
}
