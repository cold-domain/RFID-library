package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图书状态变更历史实体类
 */
@Data
@TableName("book_status_history")
public class BookStatusHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    private Long bookId;

    private String oldStatus;

    private String newStatus;

    private String changeReason;

    private Long operatorId;

    private String rfidDeviceId;

    private String rfidDeviceIp;

    private LocalDateTime changeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String remark;
}
