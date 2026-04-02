package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String studentId;

    private String username;

    private String password;

    private String realName;

    private Integer gender;

    private LocalDate birthDate;

    private String phone;

    private String email;

    private String avatarUrl;

    private String readerType;

    private String department;

    private String major;

    private LocalDate enrollmentDate;

    private LocalDate cardExpireDate;

    private Integer accountStatus;

    private Integer currentBorrowCount;

    private Integer maxBorrowCount;

    private BigDecimal fineAmount;

    private LocalDateTime lastBorrowTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime lastLoginTime;

    private Integer loginCount;

    @TableLogic
    private Integer isDeleted;

    private String wechatOpenid;

    private String remark;
}
