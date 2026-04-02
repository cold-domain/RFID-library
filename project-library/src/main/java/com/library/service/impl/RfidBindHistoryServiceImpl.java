package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.RfidBindHistory;
import com.library.mapper.RfidBindHistoryMapper;
import com.library.service.RfidBindHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RFID绑定历史服务实现类
 */
@Service
public class RfidBindHistoryServiceImpl extends ServiceImpl<RfidBindHistoryMapper, RfidBindHistory> implements RfidBindHistoryService {

    @Override
    public List<RfidBindHistory> getHistoryByBookId(Long bookId) {
        LambdaQueryWrapper<RfidBindHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RfidBindHistory::getBookId, bookId);
        wrapper.orderByDesc(RfidBindHistory::getBindTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void recordBind(Long bookId, String rfidTag, Long operatorId) {
        RfidBindHistory history = new RfidBindHistory();
        history.setBookId(bookId);
        history.setRfidTag(rfidTag);
        history.setBindTime(LocalDateTime.now());
        history.setOperatorId(operatorId);
        history.setOperationType("bind");
        baseMapper.insert(history);
    }

    @Override
    public void recordUnbind(Long bookId, String rfidTag, Long operatorId) {
        // 更新最近一条绑定记录的解绑时间
        LambdaQueryWrapper<RfidBindHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RfidBindHistory::getBookId, bookId);
        wrapper.eq(RfidBindHistory::getRfidTag, rfidTag);
        wrapper.isNull(RfidBindHistory::getUnbindTime);
        wrapper.orderByDesc(RfidBindHistory::getBindTime);
        wrapper.last("LIMIT 1");
        RfidBindHistory existing = baseMapper.selectOne(wrapper);
        if (existing != null) {
            RfidBindHistory update = new RfidBindHistory();
            update.setId(existing.getId());
            update.setUnbindTime(LocalDateTime.now());
            baseMapper.updateById(update);
        }
        // 插入解绑记录
        RfidBindHistory history = new RfidBindHistory();
        history.setBookId(bookId);
        history.setRfidTag(rfidTag);
        history.setBindTime(LocalDateTime.now());
        history.setOperatorId(operatorId);
        history.setOperationType("unbind");
        baseMapper.insert(history);
    }
}
