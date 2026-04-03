package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.BorrowRecord;
import com.library.service.BookService;
import com.library.service.BorrowRecordService;
import com.library.vo.BookVO;
import com.library.vo.BorrowRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 借阅管理控制器（馆员权限）
 */
@RestController
@RequestMapping("/librarian/borrows")
public class BorrowController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private BookService bookService;

    /**
     * 搜索图书（供借书弹窗选择使用）
     */
    @GetMapping("/search-books")
    @PreAuthorize("hasAuthority('borrow:view')")
    public Result<?> searchBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        IPage<BookVO> page = bookService.getBookPage(pageNum, pageSize, keyword, categoryId, status);
        return Result.success(page);
    }

    /**
     * 借书操作
     */
    @PostMapping("/borrow")
    @PreAuthorize("hasAuthority('borrow:operate')")
    public Result<?> borrow(@RequestParam Long userId, @RequestParam Long bookId) {
        Long operatorId = ContextHolder.getCurrentUserId();
        BorrowRecord record = borrowRecordService.borrowBook(userId, bookId, operatorId);
        return Result.success("借书成功", record);
    }

    /**
     * 还书操作
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('borrow:operate')")
    public Result<?> returnBook(@PathVariable Long id) {
        Long operatorId = ContextHolder.getCurrentUserId();
        borrowRecordService.returnBook(id, operatorId);
        return Result.success("还书成功");
    }

    /**
     * 续借操作
     */
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAuthority('borrow:operate')")
    public Result<?> renew(@PathVariable Long id) {
        Long operatorId = ContextHolder.getCurrentUserId();
        borrowRecordService.renewBook(id, operatorId);
        return Result.success("续借成功");
    }

    /**
     * 分页查询所有借阅记录
     */
    @GetMapping
    @PreAuthorize("hasAuthority('borrow:view')")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        IPage<BorrowRecordVO> page = borrowRecordService.getBorrowRecordPage(pageNum, pageSize, userId, status);
        return Result.success(page);
    }

    /**
     * 手动触发逾期检查
     */
    @PostMapping("/check-overdue")
    @PreAuthorize("hasAuthority('borrow:operate')")
    public Result<?> checkOverdue() {
        borrowRecordService.checkOverdue();
        return Result.success("逾期检查完成");
    }
}
