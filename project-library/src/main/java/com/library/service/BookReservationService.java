package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.BookReservation;
import com.library.vo.ReservationVO;

/**
 * 图书预约服务接口
 */
public interface BookReservationService extends IService<BookReservation> {

    /**
     * 预约图书
     */
    BookReservation reserveBook(Long userId, Long bookId);

    /**
     * 取消预约
     */
    void cancelReservation(Long reservationId, Long userId);

    /**
     * 取书确认
     */
    void confirmPickup(Long reservationId, Long operatorId);

    /**
     * 分页查询预约记录
     */
    IPage<ReservationVO> getReservationPage(int pageNum, int pageSize, Long userId, String status);

    /**
     * 检查并处理过期预约
     */
    void checkExpiredReservations();
}
