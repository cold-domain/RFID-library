package com.library.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 图书视图对象（字段名匹配前端）
 */
@Data
public class BookVO {

    private Long id;

    private String title;

    private String author;

    private String isbn;

    private String publisher;

    private LocalDate publishDate;

    private Long categoryId;

    private String categoryName;

    private String location;

    private String description;

    private String rfidTag;

    private String status;
}
