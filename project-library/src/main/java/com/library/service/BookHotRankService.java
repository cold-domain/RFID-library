package com.library.service;

import com.library.vo.HotBookVO;

import java.util.List;

/**
 * 图书热门榜服务
 */
public interface BookHotRankService {

    /**
     * 记录一次成功借阅，更新 Redis 热门榜。
     */
    void recordBorrow(Long bookId);

    /**
     * 获取热门图书榜。
     */
    List<HotBookVO> getHotBooks(int limit);
}
