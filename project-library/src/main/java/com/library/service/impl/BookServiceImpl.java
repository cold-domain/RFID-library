package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.dto.BookDTO;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.mapper.BookMapper;
import com.library.service.BookCategoryService;
import com.library.service.BookService;
import com.library.service.BookStatusHistoryService;
import com.library.service.RfidBindHistoryService;
import com.library.vo.BookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    @Lazy
    private RfidBindHistoryService rfidBindHistoryService;

    @Autowired
    @Lazy
    private BookStatusHistoryService bookStatusHistoryService;

    @Autowired
    @Lazy
    private BookCategoryService bookCategoryService;

    @Override
    public IPage<BookVO> getBookPage(int pageNum, int pageSize, String keyword, Long categoryId, String status) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Book::getBookName, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword)
            );
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Book::getCollectionStatus, status);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        IPage<Book> bookPage = baseMapper.selectPage(page, wrapper);

        // 批量查询分类名称，避免N+1
        List<Book> books = bookPage.getRecords();
        Map<Long, String> categoryNameMap = buildCategoryNameMap(books);

        return bookPage.convert(book -> entityToVO(book, categoryNameMap));
    }

    @Override
    public BookVO getBookDetail(Long id) {
        Book book = baseMapper.selectById(id);
        if (book == null) {
            return null;
        }
        Map<Long, String> categoryNameMap = buildCategoryNameMap(Collections.singletonList(book));
        return entityToVO(book, categoryNameMap);
    }

    @Override
    @Transactional
    public void addBook(BookDTO dto, Long creatorId) {
        Book book = dtoToEntity(dto);
        book.setCollectionStatus("on_shelf");
        book.setIsBorrowable(1);
        book.setEntryDate(LocalDate.now());
        book.setCreatorId(creatorId);
        baseMapper.insert(book);
    }

    @Override
    @Transactional
    public void updateBook(Long id, BookDTO dto, Long updaterId) {
        Book existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("图书不存在");
        }
        Book book = dtoToEntity(dto);
        book.setBookId(id);
        book.setUpdaterId(updaterId);
        baseMapper.updateById(book);
    }

    @Override
    public Book getByIsbn(String isbn) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsbn, isbn);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public Book getByRfidTag(String rfidTag) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getRfidTag, rfidTag);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void bindRfidTag(Long bookId, String rfidTag, Long operatorId) {
        Book book = baseMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (StringUtils.hasText(book.getRfidTag())) {
            throw new BusinessException("该图书已绑定RFID标签，请先解绑");
        }
        Book existingBook = getByRfidTag(rfidTag);
        if (existingBook != null) {
            throw new BusinessException("该RFID标签已被其他图书使用");
        }
        Book update = new Book();
        update.setBookId(bookId);
        update.setRfidTag(rfidTag);
        baseMapper.updateById(update);
        rfidBindHistoryService.recordBind(bookId, rfidTag, operatorId);
    }

    @Override
    @Transactional
    public void unbindRfidTag(Long bookId, Long operatorId) {
        Book book = baseMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (!StringUtils.hasText(book.getRfidTag())) {
            throw new BusinessException("该图书未绑定RFID标签");
        }
        String oldRfidTag = book.getRfidTag();
        Book update = new Book();
        update.setBookId(bookId);
        update.setRfidTag("");
        baseMapper.updateById(update);
        rfidBindHistoryService.recordUnbind(bookId, oldRfidTag, operatorId);
    }

    @Override
    @Transactional
    public void updateCollectionStatus(Long bookId, String status, Long operatorId) {
        Book book = baseMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        String oldStatus = book.getCollectionStatus();
        Book update = new Book();
        update.setBookId(bookId);
        update.setCollectionStatus(status);
        baseMapper.updateById(update);
        bookStatusHistoryService.recordStatusChange(bookId, oldStatus, status, "手动变更状态", operatorId);
    }

    /**
     * 批量构建分类ID→分类名称映射
     */
    private Map<Long, String> buildCategoryNameMap(List<Book> books) {
        List<Long> categoryIds = books.stream()
                .map(Book::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BookCategory> categories = bookCategoryService.listByIds(categoryIds);
        return categories.stream()
                .collect(Collectors.toMap(BookCategory::getCategoryId, BookCategory::getCategoryName));
    }

    /**
     * Entity → VO 转换
     */
    private BookVO entityToVO(Book book, Map<Long, String> categoryNameMap) {
        BookVO vo = new BookVO();
        vo.setId(book.getBookId());
        vo.setTitle(book.getBookName());
        vo.setAuthor(book.getAuthor());
        vo.setIsbn(book.getIsbn());
        vo.setPublisher(book.getPublisher());
        vo.setPublishDate(book.getPublishDate());
        vo.setCategoryId(book.getCategoryId());
        vo.setCategoryName(categoryNameMap.getOrDefault(book.getCategoryId(), null));
        vo.setLocation(book.getShelfLocation());
        vo.setDescription(book.getDescription());
        vo.setRfidTag(book.getRfidTag());
        vo.setStatus(book.getCollectionStatus());
        return vo;
    }

    /**
     * DTO → Entity 转换
     */
    private Book dtoToEntity(BookDTO dto) {
        Book book = new Book();
        book.setBookName(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublishDate(dto.getPublishDate());
        book.setCategoryId(dto.getCategoryId());
        book.setShelfLocation(dto.getLocation());
        book.setDescription(dto.getDescription());
        return book;
    }
}
