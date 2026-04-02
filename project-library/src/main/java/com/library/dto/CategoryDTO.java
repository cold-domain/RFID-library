package com.library.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 图书分类请求DTO（字段名匹配前端）
 */
@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId;

    private Integer sort;

    private Integer status;
}
