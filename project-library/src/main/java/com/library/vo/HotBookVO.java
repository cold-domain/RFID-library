package com.library.vo;

import lombok.Data;

/**
 * 热门图书榜视图对象
 */
@Data
public class HotBookVO {

    private Long id;

    private Integer rank;

    private String title;

    private String author;

    private String isbn;

    private String categoryName;

    private String status;

    private Integer borrowCount;
}
