package com.library.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRoleAssignmentVO {

    private Long roleId;

    private String roleName;

    private String roleCode;

    private LocalDate expireDate;
}
