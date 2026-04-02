package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 */
@Data
@TableName("borrow_records")
public class BorrowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "borrow_record_id", type = IdType.AUTO)
    private Long borrowRecordId;

    private String borrowNo;

    private Long userId;

    private Long bookId;

    private LocalDateTime borrowTime;

    private LocalDate dueDate;

    private LocalDateTime returnTime;

    private String borrowType;

    private Integer renewCount;

    private String borrowStatus;

    private Integer isOverdue;

    private Long operatorId;

    private LocalDateTime operationTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    private String remark;

    private String rfidDeviceId;

    private String rfidDeviceIp;

    private String rfidReadStatus;

    private LocalDateTime rfidReadTime;
}
