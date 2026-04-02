package com.library.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 预约记录视图对象（字段名匹配前端）
 */
@Data
public class ReservationVO {

    private Long id;

    private String username;

    private String bookTitle;

    private LocalDate reserveDate;

    private LocalDate expireDate;

    private String status;
}
