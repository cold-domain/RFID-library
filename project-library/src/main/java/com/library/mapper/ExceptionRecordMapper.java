package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.ExceptionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常记录Mapper接口
 */
@Mapper
public interface ExceptionRecordMapper extends BaseMapper<ExceptionRecord> {
}
