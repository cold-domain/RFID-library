package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.service.BookCategoryService;
import com.library.service.BookHotRankService;
import com.library.service.BookService;
import com.library.vo.HotBookVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 Redis Sorted Set 的图书热门榜服务
 */
@Slf4j
@Service
public class BookHotRankServiceImpl implements BookHotRankService {

    private static final String HOT_BOOK_RANK_KEY = "library:books:hot:rank";
    private static final int DEFAULT_LIMIT = 10;
    private static final int WARMUP_LIMIT = 50;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookCategoryService bookCategoryService;

    @Override
    public void recordBorrow(Long bookId) {
        if (bookId == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForZSet().incrementScore(HOT_BOOK_RANK_KEY, String.valueOf(bookId), 1D);
        } catch (Exception ex) {
            log.warn("更新 Redis 图书热门榜失败，bookId={}", bookId, ex);
        }
    }

    @Override
    public List<HotBookVO> getHotBooks(int limit) {
        int finalLimit = limit > 0 ? limit : DEFAULT_LIMIT;
        List<HotBookVO> hotBooks = getHotBooksFromRedis(finalLimit);
        if (!hotBooks.isEmpty()) {
            return hotBooks;
        }

        warmupRedisRank(Math.max(finalLimit, WARMUP_LIMIT));
        hotBooks = getHotBooksFromRedis(finalLimit);
        if (!hotBooks.isEmpty()) {
            return hotBooks;
        }

        return getHotBooksFromDatabase(finalLimit);
    }

    private List<HotBookVO> getHotBooksFromRedis(int limit) {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    stringRedisTemplate.opsForZSet().reverseRangeWithScores(HOT_BOOK_RANK_KEY, 0, limit - 1);
            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> bookIds = tuples.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .filter(Objects::nonNull)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            if (bookIds.isEmpty()) {
                return Collections.emptyList();
            }

            Map<Long, Book> bookMap = getBookMap(bookIds);
            Map<Long, String> categoryNameMap = getCategoryNameMap(bookMap.values());

            List<HotBookVO> hotBooks = new ArrayList<>();
            int rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String value = tuple.getValue();
                if (value == null) {
                    continue;
                }
                Book book = bookMap.get(Long.valueOf(value));
                if (book == null) {
                    continue;
                }
                hotBooks.add(toHotBookVO(book, categoryNameMap.get(book.getCategoryId()), tuple.getScore(), rank++));
            }
            return hotBooks;
        } catch (Exception ex) {
            log.warn("从 Redis 获取图书热门榜失败，回退数据库查询", ex);
            return Collections.emptyList();
        }
    }

    private void warmupRedisRank(int limit) {
        try {
            List<Book> books = queryTopBooks(limit);
            if (books.isEmpty()) {
                return;
            }
            for (Book book : books) {
                int score = book.getTotalBorrowCount() == null ? 0 : book.getTotalBorrowCount();
                if (score > 0) {
                    stringRedisTemplate.opsForZSet().add(HOT_BOOK_RANK_KEY, String.valueOf(book.getBookId()), score);
                }
            }
        } catch (Exception ex) {
            log.warn("Redis 热门榜预热失败", ex);
        }
    }

    private List<HotBookVO> getHotBooksFromDatabase(int limit) {
        List<Book> books = queryTopBooks(limit);
        if (books.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> categoryNameMap = getCategoryNameMap(books);
        List<HotBookVO> hotBooks = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            hotBooks.add(toHotBookVO(
                    book,
                    categoryNameMap.get(book.getCategoryId()),
                    book.getTotalBorrowCount() == null ? 0D : book.getTotalBorrowCount().doubleValue(),
                    i + 1
            ));
        }
        return hotBooks;
    }

    private List<Book> queryTopBooks(int limit) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Book::getTotalBorrowCount)
                .gt(Book::getTotalBorrowCount, 0)
                .orderByDesc(Book::getTotalBorrowCount)
                .orderByDesc(Book::getLastBorrowTime)
                .last("limit " + limit);
        return bookService.list(wrapper);
    }

    private Map<Long, Book> getBookMap(List<Long> bookIds) {
        List<Book> books = bookService.listByIds(bookIds);
        Map<Long, Book> originalMap = books.stream()
                .collect(Collectors.toMap(Book::getBookId, book -> book));
        Map<Long, Book> orderedMap = new LinkedHashMap<>();
        for (Long bookId : bookIds) {
            Book book = originalMap.get(bookId);
            if (book != null) {
                orderedMap.put(bookId, book);
            }
        }
        return orderedMap;
    }

    private Map<Long, String> getCategoryNameMap(Iterable<Book> books) {
        List<Long> categoryIds = new ArrayList<>();
        for (Book book : books) {
            if (book != null && book.getCategoryId() != null) {
                categoryIds.add(book.getCategoryId());
            }
        }
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<BookCategory> categories = bookCategoryService.listByIds(categoryIds.stream().distinct().collect(Collectors.toList()));
        return categories.stream()
                .collect(Collectors.toMap(BookCategory::getCategoryId, BookCategory::getCategoryName));
    }

    private HotBookVO toHotBookVO(Book book, String categoryName, Double score, int rank) {
        HotBookVO vo = new HotBookVO();
        vo.setId(book.getBookId());
        vo.setRank(rank);
        vo.setTitle(book.getBookName());
        vo.setAuthor(book.getAuthor());
        vo.setIsbn(book.getIsbn());
        vo.setCategoryName(categoryName);
        vo.setStatus(book.getCollectionStatus());
        vo.setBorrowCount(score == null ? 0 : score.intValue());
        return vo;
    }
}
