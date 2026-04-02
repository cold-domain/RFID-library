package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 罚款记录实体类
 */
@Data
@TableName("fine_records")
public class FineRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "fine_id", type = IdType.AUTO)
    private Long fineId;

    private Long userId;

    private Long borrowRecordId;

    private String fineType;

    private BigDecimal fineAmount;

    private BigDecimal paidAmount;

    private String fineStatus;

    private LocalDateTime fineTime;

    private LocalDateTime payTime;

    private Integer overdueDays;

    private Long operatorId;

    private String waiveReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String remark;
}
