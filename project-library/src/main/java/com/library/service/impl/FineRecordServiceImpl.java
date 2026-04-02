package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.FineRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.FineRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.FineRecordService;
import com.library.vo.FineRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 罚款记录服务实现类
 */
@Service
public class FineRecordServiceImpl extends ServiceImpl<FineRecordMapper, FineRecord> implements FineRecordService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Override
    @Transactional
    public FineRecord createFine(Long userId, Long borrowRecordId, String fineType, BigDecimal amount, int overdueDays, Long operatorId) {
        FineRecord fine = new FineRecord();
        fine.setUserId(userId);
        fine.setBorrowRecordId(borrowRecordId);
        fine.setFineType(fineType);
        fine.setFineAmount(amount);
        fine.setPaidAmount(BigDecimal.ZERO);
        fine.setFineStatus("unpaid");
        fine.setFineTime(LocalDateTime.now());
        fine.setOverdueDays(overdueDays);
        fine.setOperatorId(operatorId);
        baseMapper.insert(fine);

        // 更新用户欠款金额
        User user = userMapper.selectById(userId);
        if (user != null) {
            User userUpdate = new User();
            userUpdate.setUserId(userId);
            BigDecimal currentFine = user.getFineAmount() == null ? BigDecimal.ZERO : user.getFineAmount();
            userUpdate.setFineAmount(currentFine.add(amount));
            userMapper.updateById(userUpdate);
        }
        return fine;
    }

    @Override
    @Transactional
    public void payFine(Long fineId, BigDecimal amount, Long operatorId) {
        FineRecord fine = baseMapper.selectById(fineId);
        if (fine == null) {
            throw new BusinessException("罚款记录不存在");
        }
        if ("paid".equals(fine.getFineStatus())) {
            throw new BusinessException("该罚款已支付");
        }

        BigDecimal newPaidAmount = fine.getPaidAmount().add(amount);
        FineRecord update = new FineRecord();
        update.setFineId(fineId);
        update.setPaidAmount(newPaidAmount);
        update.setPayTime(LocalDateTime.now());
        update.setOperatorId(operatorId);
        // 判断是否全额支付
        if (newPaidAmount.compareTo(fine.getFineAmount()) >= 0) {
            update.setFineStatus("paid");
        }
        baseMapper.updateById(update);

        // 更新用户欠款金额
        User user = userMapper.selectById(fine.getUserId());
        if (user != null) {
            User userUpdate = new User();
            userUpdate.setUserId(user.getUserId());
            BigDecimal currentFine = user.getFineAmount() == null ? BigDecimal.ZERO : user.getFineAmount();
            userUpdate.setFineAmount(currentFine.subtract(amount).max(BigDecimal.ZERO));
            userMapper.updateById(userUpdate);
        }
    }

    @Override
    @Transactional
    public void waiveFine(Long fineId, String reason, Long operatorId) {
        FineRecord fine = baseMapper.selectById(fineId);
        if (fine == null) {
            throw new BusinessException("罚款记录不存在");
        }
        BigDecimal waiveAmount = fine.getFineAmount().subtract(fine.getPaidAmount());

        FineRecord update = new FineRecord();
        update.setFineId(fineId);
        update.setFineStatus("waived");
        update.setWaiveReason(reason);
        update.setOperatorId(operatorId);
        baseMapper.updateById(update);

        // 更新用户欠款金额
        User user = userMapper.selectById(fine.getUserId());
        if (user != null) {
            User userUpdate = new User();
            userUpdate.setUserId(user.getUserId());
            BigDecimal currentFine = user.getFineAmount() == null ? BigDecimal.ZERO : user.getFineAmount();
            userUpdate.setFineAmount(currentFine.subtract(waiveAmount).max(BigDecimal.ZERO));
            userMapper.updateById(userUpdate);
        }
    }

    @Override
    public IPage<FineRecordVO> getFinePage(int pageNum, int pageSize, Long userId, String fineStatus) {
        Page<FineRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FineRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(FineRecord::getUserId, userId);
        }
        if (StringUtils.hasText(fineStatus)) {
            wrapper.eq(FineRecord::getFineStatus, fineStatus);
        }
        wrapper.orderByDesc(FineRecord::getFineTime);
        IPage<FineRecord> recordPage = baseMapper.selectPage(page, wrapper);

        Map<Long, String> userNameMap = buildUserNameMap(recordPage.getRecords());
        Map<Long, String> bookNameMap = buildBookNameMap(recordPage.getRecords());
        return recordPage.convert(record -> entityToVO(record, userNameMap, bookNameMap));
    }

    /**
     * 批量构建用户ID→用户名映射
     */
    private Map<Long, String> buildUserNameMap(List<FineRecord> records) {
        List<Long> userIds = records.stream()
                .map(FineRecord::getUserId)
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
     * 批量构建 borrowRecordId → 图书名称映射
     */
    private Map<Long, String> buildBookNameMap(List<FineRecord> records) {
        List<Long> borrowRecordIds = records.stream()
                .map(FineRecord::getBorrowRecordId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (borrowRecordIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BorrowRecord> borrowRecords = borrowRecordMapper.selectBatchIds(borrowRecordIds);
        List<Long> bookIds = borrowRecords.stream()
                .map(BorrowRecord::getBookId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> bookIdToName = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getBookId, Book::getBookName));
        return borrowRecords.stream()
                .filter(br -> br.getBookId() != null && bookIdToName.containsKey(br.getBookId()))
                .collect(Collectors.toMap(BorrowRecord::getBorrowRecordId, br -> bookIdToName.get(br.getBookId())));
    }

    /**
     * Entity → VO 转换
     */
    private FineRecordVO entityToVO(FineRecord record, Map<Long, String> userNameMap, Map<Long, String> bookNameMap) {
        FineRecordVO vo = new FineRecordVO();
        vo.setId(record.getFineId());
        vo.setUsername(userNameMap.getOrDefault(record.getUserId(), null));
        vo.setFineType(record.getFineType());
        vo.setBookTitle(record.getBorrowRecordId() != null ? bookNameMap.getOrDefault(record.getBorrowRecordId(), null) : null);
        vo.setAmount(record.getFineAmount());
        vo.setPaidAmount(record.getPaidAmount());
        vo.setOverdueDays(record.getOverdueDays());
        vo.setFineStatus(record.getFineStatus());
        return vo;
    }

    @Override
    public BigDecimal getUnpaidAmount(Long userId) {
        LambdaQueryWrapper<FineRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FineRecord::getUserId, userId);
        wrapper.eq(FineRecord::getFineStatus, "unpaid");
        List<FineRecord> fines = baseMapper.selectList(wrapper);
        return fines.stream()
                .map(f -> f.getFineAmount().subtract(f.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
