package com.library.vo;

import lombok.Data;

import java.util.List;

/**
 * 图书分类视图对象（字段名匹配前端）
 */
@Data
public class CategoryVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer sort;

    private Integer status;

    private List<CategoryVO> children;
}
