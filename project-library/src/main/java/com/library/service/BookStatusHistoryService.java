package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.BookStatusHistory;

import java.util.List;

/**
 * 图书状态变更历史服务接口
 */
public interface BookStatusHistoryService extends IService<BookStatusHistory> {

    /**
     * 记录状态变更
     */
    void recordStatusChange(Long bookId, String oldStatus, String newStatus, String reason, Long operatorId);

    /**
     * 根据图书ID查询状态变更历史
     */
    List<BookStatusHistory> getHistoryByBookId(Long bookId);
}
