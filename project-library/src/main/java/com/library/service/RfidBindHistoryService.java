package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.RfidBindHistory;

import java.util.List;

/**
 * RFID绑定历史服务接口
 */
public interface RfidBindHistoryService extends IService<RfidBindHistory> {

    /**
     * 根据图书ID查询绑定历史
     */
    List<RfidBindHistory> getHistoryByBookId(Long bookId);

    /**
     * 记录绑定操作
     */
    void recordBind(Long bookId, String rfidTag, Long operatorId);

    /**
     * 记录解绑操作
     */
    void recordUnbind(Long bookId, String rfidTag, Long operatorId);
}
