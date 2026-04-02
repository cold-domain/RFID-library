package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookReservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书预约Mapper接口
 */
@Mapper
public interface BookReservationMapper extends BaseMapper<BookReservation> {
}
