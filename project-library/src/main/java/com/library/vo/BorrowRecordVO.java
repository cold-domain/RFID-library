package com.library.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 借阅记录视图对象（字段名匹配前端）
 */
@Data
public class BorrowRecordVO {

    private Long id;

    private String username;

    private String bookTitle;

    private String isbn;

    private LocalDate borrowDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    private Integer renewCount;

    private String status;
}
