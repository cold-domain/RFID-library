package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.BorrowRecord;
import com.library.vo.BorrowRecordVO;

/**
 * 借阅记录服务接口
 */
public interface BorrowRecordService extends IService<BorrowRecord> {

    /**
     * 借书
     */
    BorrowRecord borrowBook(Long userId, Long bookId, Long operatorId);

    /**
     * 还书
     */
    void returnBook(Long borrowRecordId, Long operatorId);

    /**
     * 续借
     */
    void renewBook(Long borrowRecordId, Long operatorId);

    /**
     * 分页查询借阅记录
     */
    IPage<BorrowRecordVO> getBorrowRecordPage(int pageNum, int pageSize, Long userId, String status);

    /**
     * 查询用户当前借阅中的记录数
     */
    int countCurrentBorrowing(Long userId);

    /**
     * 检查并标记逾期记录
     */
    void checkOverdue();
}
