package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.dto.BookDTO;
import com.library.entity.BookStatusHistory;
import com.library.entity.RfidBindHistory;
import com.library.service.BookService;
import com.library.service.BookStatusHistoryService;
import com.library.service.RfidBindHistoryService;
import com.library.vo.BookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 图书管理控制器（馆员权限）
 */
@RestController
@RequestMapping("/librarian/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private RfidBindHistoryService rfidBindHistoryService;

    @Autowired
    private BookStatusHistoryService bookStatusHistoryService;

    /**
     * 分页查询图书列表
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        IPage<BookVO> page = bookService.getBookPage(pageNum, pageSize, keyword, categoryId, status);
        return Result.success(page);
    }

    /**
     * 根据ID查询图书详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        BookVO bookVO = bookService.getBookDetail(id);
        if (bookVO == null) {
            return Result.error("图书不存在");
        }
        return Result.success(bookVO);
    }

    /**
     * 新增图书
     */
    @PostMapping
    public Result<?> add(@Valid @RequestBody BookDTO bookDTO) {
        bookService.addBook(bookDTO, ContextHolder.getCurrentUserId());
        return Result.success("新增图书成功");
    }

    /**
     * 修改图书信息
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        bookService.updateBook(id, bookDTO, ContextHolder.getCurrentUserId());
        return Result.success("修改图书信息成功");
    }

    /**
     * 删除图书（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bookService.removeById(id);
        return Result.success("删除图书成功");
    }

    /**
     * 绑定RFID标签
     */
    @PostMapping("/{id}/rfid/bind")
    public Result<?> bindRfid(@PathVariable Long id, @RequestParam String rfidTag) {
        Long operatorId = ContextHolder.getCurrentUserId();
        bookService.bindRfidTag(id, rfidTag, operatorId);
        return Result.success("RFID标签绑定成功");
    }

    /**
     * 解绑RFID标签
     */
    @PostMapping("/{id}/rfid/unbind")
    public Result<?> unbindRfid(@PathVariable Long id) {
        Long operatorId = ContextHolder.getCurrentUserId();
        bookService.unbindRfidTag(id, operatorId);
        return Result.success("RFID标签解绑成功");
    }

    /**
     * 更新图书馆藏状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Long operatorId = ContextHolder.getCurrentUserId();
        bookService.updateCollectionStatus(id, status, operatorId);
        return Result.success("状态更新成功");
    }

    /**
     * 查询图书RFID绑定历史
     */
    @GetMapping("/{id}/rfid/history")
    public Result<?> rfidHistory(@PathVariable Long id) {
        List<RfidBindHistory> history = rfidBindHistoryService.getHistoryByBookId(id);
        return Result.success(history);
    }

    /**
     * 查询图书状态变更历史
     */
    @GetMapping("/{id}/status/history")
    public Result<?> statusHistory(@PathVariable Long id) {
        List<BookStatusHistory> history = bookStatusHistoryService.getHistoryByBookId(id);
        return Result.success(history);
    }
}
