package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookStatusHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书状态变更历史Mapper接口
 */
@Mapper
public interface BookStatusHistoryMapper extends BaseMapper<BookStatusHistory> {
}
