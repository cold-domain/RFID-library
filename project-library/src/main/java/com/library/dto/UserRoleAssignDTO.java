package com.library.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRoleAssignDTO {

    private Long roleId;

    private LocalDate expireDate;
}
