package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.vo.Result;
import com.library.service.BookHotRankService;
import com.library.service.BookCategoryService;
import com.library.vo.BookVO;
import com.library.vo.CategoryVO;
import com.library.vo.HotBookVO;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书公开接口控制器（无需认证）
 */
@RestController
@RequestMapping("/books/public")
public class PublicBookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookCategoryService bookCategoryService;

    @Autowired
    private BookHotRankService bookHotRankService;

    /**
     * 公开查询图书列表
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        IPage<BookVO> page = bookService.getBookPage(pageNum, pageSize, keyword, categoryId, null);
        return Result.success(page);
    }

    /**
     * 公开查询图书详情
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
     * 公开查询图书分类列表
     */
    @GetMapping("/categories")
    public Result<?> categories() {
        List<CategoryVO> categories = bookCategoryService.getCategoryTree();
        return Result.success(categories);
    }

    /**
     * 公开热门图书榜
     */
    @GetMapping("/hot")
    public Result<?> hotBooks(@RequestParam(defaultValue = "10") int limit) {
        List<HotBookVO> hotBooks = bookHotRankService.getHotBooks(limit);
        return Result.success(hotBooks);
    }
}
