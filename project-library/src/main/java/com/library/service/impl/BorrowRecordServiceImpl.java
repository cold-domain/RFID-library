package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.constant.Constants;
import com.library.common.exception.BusinessException;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BookStatusHistoryService;
import com.library.service.BorrowRecordService;
import com.library.service.UserService;
import com.library.vo.BorrowRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 借阅记录服务实现类
 */
@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    @Lazy
    private BookStatusHistoryService bookStatusHistoryService;

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId, Long operatorId) {
        // 校验用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getAccountStatus() != 1) {
            throw new BusinessException("用户账号状态异常，无法借书");
        }
        // 管理员角色不受借阅上限限制
        List<String> roles = userService.getUserRoleCodes(userId);
        boolean isAdmin = roles.contains(Constants.ROLE_SYSTEM_ADMIN) || roles.contains(Constants.ROLE_SUPER_ADMIN);
        if (!isAdmin && user.getCurrentBorrowCount() >= user.getMaxBorrowCount()) {
            throw new BusinessException("已达最大借阅数量限制");
        }
        // 校验图书
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (!Constants.BOOK_STATUS_ON_SHELF.equals(book.getCollectionStatus())) {
            throw new BusinessException("该图书当前不可借阅");
        }
        if (book.getIsBorrowable() != null && book.getIsBorrowable() == 0) {
            throw new BusinessException("该图书不允许外借");
        }

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setBorrowNo("BR" + System.currentTimeMillis());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowTime(LocalDateTime.now());
        record.setDueDate(LocalDate.now().plusDays(30)); // 默认借期30天
        record.setBorrowType("normal");
        record.setRenewCount(0);
        record.setBorrowStatus("borrowed");
        record.setIsOverdue(0);
        record.setOperatorId(operatorId);
        record.setOperationTime(LocalDateTime.now());
        baseMapper.insert(record);

        // 更新图书状态
        Book bookUpdate = new Book();
        bookUpdate.setBookId(bookId);
        bookUpdate.setCollectionStatus(Constants.BOOK_STATUS_BORROWED);
        bookUpdate.setCurrentBorrowerId(userId);
        bookUpdate.setDueDate(record.getDueDate());
        bookUpdate.setLastBorrowTime(LocalDateTime.now());
        bookUpdate.setTotalBorrowCount(book.getTotalBorrowCount() == null ? 1 : book.getTotalBorrowCount() + 1);
        bookMapper.updateById(bookUpdate);

        // 更新用户借阅数量
        User userUpdate = new User();
        userUpdate.setUserId(userId);
        userUpdate.setCurrentBorrowCount(user.getCurrentBorrowCount() + 1);
        userUpdate.setLastBorrowTime(LocalDateTime.now());
        userMapper.updateById(userUpdate);

        // 记录图书状态变更
        bookStatusHistoryService.recordStatusChange(bookId, Constants.BOOK_STATUS_ON_SHELF, Constants.BOOK_STATUS_BORROWED, "读者借阅", operatorId);

        return record;
    }

    @Override
    @Transactional
    public void returnBook(Long borrowRecordId, Long operatorId) {
        BorrowRecord record = baseMapper.selectById(borrowRecordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (!"borrowed".equals(record.getBorrowStatus()) && !"overdue".equals(record.getBorrowStatus())) {
            throw new BusinessException("该记录状态不允许还书");
        }

        // 更新借阅记录
        BorrowRecord update = new BorrowRecord();
        update.setBorrowRecordId(borrowRecordId);
        update.setReturnTime(LocalDateTime.now());
        update.setBorrowStatus("returned");
        update.setOperatorId(operatorId);
        update.setOperationTime(LocalDateTime.now());
        baseMapper.updateById(update);

        // 更新图书状态
        Book bookUpdate = new Book();
        bookUpdate.setBookId(record.getBookId());
        bookUpdate.setCollectionStatus(Constants.BOOK_STATUS_ON_SHELF);
        bookUpdate.setCurrentBorrowerId(null);
        bookUpdate.setDueDate(null);
        bookUpdate.setLastReturnTime(LocalDateTime.now());
        bookMapper.updateById(bookUpdate);

        // 更新用户借阅数量
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            User userUpdate = new User();
            userUpdate.setUserId(user.getUserId());
            userUpdate.setCurrentBorrowCount(Math.max(0, user.getCurrentBorrowCount() - 1));
            userMapper.updateById(userUpdate);
        }

        // 记录图书状态变更
        bookStatusHistoryService.recordStatusChange(record.getBookId(), Constants.BOOK_STATUS_BORROWED, Constants.BOOK_STATUS_ON_SHELF, "读者归还", operatorId);
    }

    @Override
    @Transactional
    public void renewBook(Long borrowRecordId, Long operatorId) {
        BorrowRecord record = baseMapper.selectById(borrowRecordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (!"borrowed".equals(record.getBorrowStatus())) {
            throw new BusinessException("该记录状态不允许续借");
        }
        if (record.getRenewCount() >= 2) {
            throw new BusinessException("最多续借2次");
        }

        // 续借延长30天
        BorrowRecord update = new BorrowRecord();
        update.setBorrowRecordId(borrowRecordId);
        update.setDueDate(record.getDueDate().plusDays(30));
        update.setRenewCount(record.getRenewCount() + 1);
        update.setOperatorId(operatorId);
        update.setOperationTime(LocalDateTime.now());
        baseMapper.updateById(update);

        // 同步更新图书应还日期
        Book bookUpdate = new Book();
        bookUpdate.setBookId(record.getBookId());
        bookUpdate.setDueDate(record.getDueDate().plusDays(30));
        bookMapper.updateById(bookUpdate);
    }

    @Override
    public IPage<BorrowRecordVO> getBorrowRecordPage(int pageNum, int pageSize, Long userId, String status) {
        Page<BorrowRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(BorrowRecord::getUserId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BorrowRecord::getBorrowStatus, status);
        }
        wrapper.orderByDesc(BorrowRecord::getBorrowTime);
        IPage<BorrowRecord> recordPage = baseMapper.selectPage(page, wrapper);

        // 批量查询用户名和图书信息，避免N+1
        List<BorrowRecord> records = recordPage.getRecords();
        Map<Long, String> userNameMap = buildUserNameMap(records);
        Map<Long, Book> bookInfoMap = buildBookInfoMap(records);

        return recordPage.convert(record -> entityToVO(record, userNameMap, bookInfoMap));
    }

    @Override
    public int countCurrentBorrowing(Long userId) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId);
        wrapper.eq(BorrowRecord::getBorrowStatus, "borrowed");
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }

    @Override
    @Transactional
    public void checkOverdue() {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getBorrowStatus, "borrowed");
        wrapper.lt(BorrowRecord::getDueDate, LocalDate.now());
        wrapper.eq(BorrowRecord::getIsOverdue, 0);
        List<BorrowRecord> overdueList = baseMapper.selectList(wrapper);
        for (BorrowRecord record : overdueList) {
            BorrowRecord update = new BorrowRecord();
            update.setBorrowRecordId(record.getBorrowRecordId());
            update.setIsOverdue(1);
            update.setBorrowStatus("overdue");
            baseMapper.updateById(update);
        }
    }

    /**
     * 批量构建用户ID→用户名映射
     */
    private Map<Long, String> buildUserNameMap(List<BorrowRecord> records) {
        List<Long> userIds = records.stream()
                .map(BorrowRecord::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
    }

    /**
     * 批量构建图书ID→图书信息映射
     */
    private Map<Long, Book> buildBookInfoMap(List<BorrowRecord> records) {
        List<Long> bookIds = records.stream()
                .map(BorrowRecord::getBookId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Book> books = bookMapper.selectBatchIds(bookIds);
        return books.stream()
                .collect(Collectors.toMap(Book::getBookId, book -> book));
    }

    /**
     * Entity → VO 转换
     */
    private BorrowRecordVO entityToVO(BorrowRecord record, Map<Long, String> userNameMap, Map<Long, Book> bookInfoMap) {
        BorrowRecordVO vo = new BorrowRecordVO();
        vo.setId(record.getBorrowRecordId());
        vo.setUsername(userNameMap.getOrDefault(record.getUserId(), null));
        Book book = bookInfoMap.get(record.getBookId());
        if (book != null) {
            vo.setBookTitle(book.getBookName());
            vo.setIsbn(book.getIsbn());
        }
        vo.setBorrowDate(record.getBorrowTime() != null ? record.getBorrowTime().toLocalDate() : null);
        vo.setDueDate(record.getDueDate());
        vo.setReturnDate(record.getReturnTime() != null ? record.getReturnTime().toLocalDate() : null);
        vo.setRenewCount(record.getRenewCount());
        vo.setStatus(record.getBorrowStatus());
        return vo;
    }
}
