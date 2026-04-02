package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RFID绑定历史实体类
 */
@Data
@TableName("rfid_bind_history")
public class RfidBindHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private String rfidTag;

    private LocalDateTime bindTime;

    private LocalDateTime unbindTime;

    private Long operatorId;

    private String operationType;

    private String remark;
}
