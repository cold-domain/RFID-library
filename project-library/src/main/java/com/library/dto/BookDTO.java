package com.library.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 图书请求DTO（字段名匹配前端）
 */
@Data
public class BookDTO {

    @NotBlank(message = "书名不能为空")
    private String title;

    @NotBlank(message = "作者不能为空")
    private String author;

    @NotBlank(message = "ISBN不能为空")
    private String isbn;

    private String publisher;

    private LocalDate publishDate;

    private Long categoryId;

    private String location;

    private String description;
}
