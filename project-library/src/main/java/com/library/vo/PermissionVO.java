package com.library.vo;

import lombok.Data;

import java.util.List;

/**
 * 权限视图对象（字段名匹配前端，支持树形结构）
 */
@Data
public class PermissionVO {

    private Long id;

    private String name;

    private String permissionCode;

    private String type;

    private Long parentId;

    private String url;

    private Integer sort;

    private Integer status;

    private List<PermissionVO> children;
}
