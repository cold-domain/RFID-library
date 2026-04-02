package com.library.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户视图对象（字段名匹配前端）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private Integer status;

    /** 角色名称列表（显示用） */
    private List<String> roles;

    /** 角色ID列表（分配角色回显用） */
    private List<Long> roleIds;

    private String createTime;
}
