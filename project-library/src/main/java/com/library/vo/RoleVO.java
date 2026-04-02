package com.library.vo;

import lombok.Data;

/**
 * 角色视图对象（字段名匹配前端）
 */
@Data
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer status;

    private Integer isSystemRole;

    private Integer sortOrder;

    private String createTime;
}
