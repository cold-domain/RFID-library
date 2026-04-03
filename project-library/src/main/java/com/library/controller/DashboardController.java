package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.vo.Result;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.entity.BorrowRecord;
import com.library.service.BookCategoryService;
import com.library.service.BookHotRankService;
import com.library.service.BookService;
import com.library.service.BorrowRecordService;
import com.library.service.UserService;
import com.library.vo.HotBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制中心控制器（所有已登录用户可访问）
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private BookCategoryService bookCategoryService;

    @Autowired
    private BookHotRankService bookHotRankService;

    /**
     * 获取控制中心统计数据
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard')")
    public Result<?> getStats() {
        Map<String, Object> data = new HashMap<>();

        // ========== 基础统计 ==========
        data.put("totalBooks", bookService.count());
        data.put("totalUsers", userService.count());

        // 当前借阅中
        LambdaQueryWrapper<BorrowRecord> borrowedWrapper = new LambdaQueryWrapper<>();
        borrowedWrapper.eq(BorrowRecord::getBorrowStatus, "borrowed");
        data.put("activeBorrows", borrowRecordService.count(borrowedWrapper));

        // 逾期未还
        LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.eq(BorrowRecord::getBorrowStatus, "overdue");
        data.put("overdueCount", borrowRecordService.count(overdueWrapper));

        // ========== 图书分类分布（饼图） ==========
        List<Book> allBooks = bookService.list();
        Map<Long, Long> catCountMap = allBooks.stream()
                .filter(b -> b.getCategoryId() != null)
                .collect(Collectors.groupingBy(Book::getCategoryId, Collectors.counting()));

        List<BookCategory> categories = bookCategoryService.list();
        Map<Long, String> catNameMap = categories.stream()
                .collect(Collectors.toMap(BookCategory::getCategoryId, BookCategory::getCategoryName));

        List<Map<String, Object>> categoryStats = new ArrayList<>();
        catCountMap.forEach((catId, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", catNameMap.getOrDefault(catId, "未分类"));
            item.put("value", count);
            categoryStats.add(item);
        });
        categoryStats.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));
        if (categoryStats.size() > 10) {
            data.put("categoryStats", categoryStats.subList(0, 10));
        } else {
            data.put("categoryStats", categoryStats);
        }

        // ========== 近7天借阅趋势（柱状图） ==========
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        LambdaQueryWrapper<BorrowRecord> trendWrapper = new LambdaQueryWrapper<>();
        trendWrapper.ge(BorrowRecord::getBorrowTime, sevenDaysAgo);
        List<BorrowRecord> recentBorrows = borrowRecordService.list(trendWrapper);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, Long> dailyCount = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyCount.put(LocalDate.now().minusDays(i).format(dateFmt), 0L);
        }
        for (BorrowRecord br : recentBorrows) {
            if (br.getBorrowTime() != null) {
                String dateKey = br.getBorrowTime().toLocalDate().format(dateFmt);
                dailyCount.merge(dateKey, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> borrowTrend = new ArrayList<>();
        dailyCount.forEach((date, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("count", count);
            borrowTrend.add(item);
        });
        data.put("borrowTrend", borrowTrend);

        List<HotBookVO> hotBooks = bookHotRankService.getHotBooks(10);
        data.put("hotBooks", hotBooks);

        return Result.success(data);
    }
}
