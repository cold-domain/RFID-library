package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.RfidBindHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * RFID绑定历史Mapper接口
 */
@Mapper
public interface RfidBindHistoryMapper extends BaseMapper<RfidBindHistory> {
}
