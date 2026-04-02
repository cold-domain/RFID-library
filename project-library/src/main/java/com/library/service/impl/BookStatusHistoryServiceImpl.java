package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.BookStatusHistory;
import com.library.mapper.BookStatusHistoryMapper;
import com.library.service.BookStatusHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书状态变更历史服务实现类
 */
@Service
public class BookStatusHistoryServiceImpl extends ServiceImpl<BookStatusHistoryMapper, BookStatusHistory> implements BookStatusHistoryService {

    @Override
    public void recordStatusChange(Long bookId, String oldStatus, String newStatus, String reason, Long operatorId) {
        BookStatusHistory history = new BookStatusHistory();
        history.setBookId(bookId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangeReason(reason);
        history.setOperatorId(operatorId);
        history.setChangeTime(LocalDateTime.now());
        baseMapper.insert(history);
    }

    @Override
    public List<BookStatusHistory> getHistoryByBookId(Long bookId) {
        LambdaQueryWrapper<BookStatusHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookStatusHistory::getBookId, bookId);
        wrapper.orderByDesc(BookStatusHistory::getChangeTime);
        return baseMapper.selectList(wrapper);
    }
}
