package com.library.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 罚款记录视图对象（字段名匹配前端）
 */
@Data
public class FineRecordVO {

    private Long id;

    private String username;

    private String fineType;

    private String bookTitle;

    private BigDecimal amount;

    private BigDecimal paidAmount;

    private Integer overdueDays;

    private String fineStatus;
}
