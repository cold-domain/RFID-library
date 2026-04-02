package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.FineRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 罚款记录Mapper接口
 */
@Mapper
public interface FineRecordMapper extends BaseMapper<FineRecord> {
}
