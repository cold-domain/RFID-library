package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书实体类
 */
@Data
@TableName("books")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "book_id", type = IdType.AUTO)
    private Long bookId;

    private String bookName;

    private String author;

    private String isbn;

    private String publisher;

    private LocalDate publishDate;

    private Integer pageCount;

    private BigDecimal price;

    private String description;

    private String coverUrl;

    private Long categoryId;

    private String tags;

    private String shelfLocation;

    private String rfidTag;

    private String barcode;

    private String callNumber;

    private LocalDate entryDate;

    private String source;

    private String donor;

    private String collectionStatus;

    private Integer isBorrowable;

    private Long currentBorrowerId;

    private LocalDate dueDate;

    private Integer totalBorrowCount;

    private LocalDateTime lastBorrowTime;

    private LocalDateTime lastReturnTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long creatorId;

    private Long updaterId;

    @TableLogic
    private Integer isDeleted;
}
