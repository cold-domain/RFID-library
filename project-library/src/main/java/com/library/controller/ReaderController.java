package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.BookReservation;
import com.library.service.BorrowRecordService;
import com.library.service.BookReservationService;
import com.library.service.FineRecordService;
import com.library.service.UserService;
import com.library.vo.BorrowRecordVO;
import com.library.vo.FineRecordVO;
import com.library.vo.ReservationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 读者个人中心控制器（读者权限）
 */
@RestController
@RequestMapping("/reader")
public class ReaderController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private BookReservationService bookReservationService;

    @Autowired
    private FineRecordService fineRecordService;

    @Autowired
    private UserService userService;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<?> getProfile() {
        Long userId = ContextHolder.getCurrentUserId();
        var user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody com.library.entity.User user) {
        Long userId = ContextHolder.getCurrentUserId();
        user.setUserId(userId);
        user.setPassword(null);
        user.setAccountStatus(null); // 不允许自行修改状态
        user.setMaxBorrowCount(null); // 不允许自行修改借阅上限
        userService.updateById(user);
        return Result.success("个人信息修改成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        Long userId = ContextHolder.getCurrentUserId();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 查询我的借阅记录
     */
    @GetMapping("/borrows")
    public Result<?> myBorrows(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = ContextHolder.getCurrentUserId();
        IPage<BorrowRecordVO> page = borrowRecordService.getBorrowRecordPage(pageNum, pageSize, userId, status);
        return Result.success(page);
    }

    /**
     * 读者自助续借
     */
    @PostMapping("/borrows/{id}/renew")
    public Result<?> renew(@PathVariable Long id) {
        Long operatorId = ContextHolder.getCurrentUserId();
        borrowRecordService.renewBook(id, operatorId);
        return Result.success("续借成功");
    }

    /**
     * 预约图书
     */
    @PostMapping("/reservations")
    public Result<?> reserve(@RequestParam Long bookId) {
        Long userId = ContextHolder.getCurrentUserId();
        BookReservation reservation = bookReservationService.reserveBook(userId, bookId);
        return Result.success("预约成功", reservation);
    }

    /**
     * 查询我的预约记录
     */
    @GetMapping("/reservations")
    public Result<?> myReservations(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = ContextHolder.getCurrentUserId();
        // 反向映射：前端 completed → 后端 picked_up
        if ("completed".equals(status)) {
            status = "picked_up";
        }
        IPage<ReservationVO> page = bookReservationService.getReservationPage(pageNum, pageSize, userId, status);
        return Result.success(page);
    }

    /**
     * 取消预约
     */
    @PostMapping("/reservations/{id}/cancel")
    public Result<?> cancelReservation(@PathVariable Long id) {
        Long userId = ContextHolder.getCurrentUserId();
        bookReservationService.cancelReservation(id, userId);
        return Result.success("预约已取消");
    }

    /**
     * 查询我的罚款记录
     */
    @GetMapping("/fines")
    public Result<?> myFines(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String fineStatus) {
        Long userId = ContextHolder.getCurrentUserId();
        IPage<FineRecordVO> page = fineRecordService.getFinePage(pageNum, pageSize, userId, fineStatus);
        return Result.success(page);
    }

    /**
     * 读者自助缴费
     */
    @PostMapping("/fines/{id}/pay")
    public Result<?> payFine(@PathVariable Long id, @RequestParam BigDecimal amount) {
        Long userId = ContextHolder.getCurrentUserId();
        fineRecordService.payFine(id, amount, userId);
        return Result.success("缴费成功");
    }
}
