package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图书预约实体类
 */
@Data
@TableName("book_reservations")
public class BookReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "reservation_id", type = IdType.AUTO)
    private Long reservationId;

    private Long userId;

    private Long bookId;

    private LocalDateTime reservationTime;

    private LocalDateTime expireTime;

    private LocalDateTime pickupDeadline;

    private LocalDateTime pickupTime;

    private LocalDateTime cancelTime;

    private String reservationStatus;

    private Integer notificationSent;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String remark;
}
