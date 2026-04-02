package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.entity.Book;
import com.library.entity.BookReservation;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BookReservationMapper;
import com.library.mapper.UserMapper;
import com.library.service.BookReservationService;
import com.library.vo.ReservationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书预约服务实现类
 */
@Service
public class BookReservationServiceImpl extends ServiceImpl<BookReservationMapper, BookReservation> implements BookReservationService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public BookReservation reserveBook(Long userId, Long bookId) {
        // 检查图书是否存在
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        // 检查是否已有有效预约
        LambdaQueryWrapper<BookReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReservation::getUserId, userId);
        wrapper.eq(BookReservation::getBookId, bookId);
        wrapper.in(BookReservation::getReservationStatus, "pending", "available");
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已预约该图书，请勿重复预约");
        }

        BookReservation reservation = new BookReservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setReservationTime(LocalDateTime.now());
        reservation.setExpireTime(LocalDateTime.now().plusDays(7)); // 预约有效期7天
        reservation.setReservationStatus("pending");
        reservation.setNotificationSent(0);
        baseMapper.insert(reservation);
        return reservation;
    }

    @Override
    public void cancelReservation(Long reservationId, Long userId) {
        BookReservation reservation = baseMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权取消他人预约");
        }
        if (!"pending".equals(reservation.getReservationStatus()) && !"available".equals(reservation.getReservationStatus())) {
            throw new BusinessException("当前状态不允许取消");
        }
        BookReservation update = new BookReservation();
        update.setReservationId(reservationId);
        update.setReservationStatus("cancelled");
        update.setCancelTime(LocalDateTime.now());
        baseMapper.updateById(update);
    }

    @Override
    public void confirmPickup(Long reservationId, Long operatorId) {
        BookReservation reservation = baseMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!"pending".equals(reservation.getReservationStatus()) && !"available".equals(reservation.getReservationStatus())) {
            throw new BusinessException("当前状态不允许取书");
        }
        BookReservation update = new BookReservation();
        update.setReservationId(reservationId);
        update.setReservationStatus("picked_up");
        update.setPickupTime(LocalDateTime.now());
        update.setOperatorId(operatorId);
        baseMapper.updateById(update);
    }

    @Override
    public IPage<ReservationVO> getReservationPage(int pageNum, int pageSize, Long userId, String status) {
        Page<BookReservation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookReservation> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(BookReservation::getUserId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BookReservation::getReservationStatus, status);
        }
        wrapper.orderByDesc(BookReservation::getReservationTime);
        IPage<BookReservation> recordPage = baseMapper.selectPage(page, wrapper);

        List<BookReservation> records = recordPage.getRecords();
        Map<Long, String> userNameMap = buildUserNameMap(records);
        Map<Long, String> bookNameMap = buildBookNameMap(records);

        return recordPage.convert(record -> entityToVO(record, userNameMap, bookNameMap));
    }

    @Override
    @Transactional
    public void checkExpiredReservations() {
        LambdaQueryWrapper<BookReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReservation::getReservationStatus, "pending");
        wrapper.lt(BookReservation::getExpireTime, LocalDateTime.now());
        List<BookReservation> expiredList = baseMapper.selectList(wrapper);
        for (BookReservation reservation : expiredList) {
            BookReservation update = new BookReservation();
            update.setReservationId(reservation.getReservationId());
            update.setReservationStatus("expired");
            baseMapper.updateById(update);
        }
    }

    /**
     * 批量构建用户ID→用户名映射
     */
    private Map<Long, String> buildUserNameMap(List<BookReservation> records) {
        List<Long> userIds = records.stream()
                .map(BookReservation::getUserId)
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
     * 批量构建图书ID→书名映射
     */
    private Map<Long, String> buildBookNameMap(List<BookReservation> records) {
        List<Long> bookIds = records.stream()
                .map(BookReservation::getBookId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Book> books = bookMapper.selectBatchIds(bookIds);
        return books.stream()
                .collect(Collectors.toMap(Book::getBookId, Book::getBookName));
    }

    /**
     * Entity → VO 转换（含状态映射：picked_up → completed）
     */
    private ReservationVO entityToVO(BookReservation record, Map<Long, String> userNameMap, Map<Long, String> bookNameMap) {
        ReservationVO vo = new ReservationVO();
        vo.setId(record.getReservationId());
        vo.setUsername(userNameMap.getOrDefault(record.getUserId(), null));
        vo.setBookTitle(bookNameMap.getOrDefault(record.getBookId(), null));
        vo.setReserveDate(record.getReservationTime() != null ? record.getReservationTime().toLocalDate() : null);
        vo.setExpireDate(record.getExpireTime() != null ? record.getExpireTime().toLocalDate() : null);
        // 状态映射：后端 picked_up → 前端 completed
        String status = record.getReservationStatus();
        vo.setStatus("picked_up".equals(status) ? "completed" : status);
        return vo;
    }
}
