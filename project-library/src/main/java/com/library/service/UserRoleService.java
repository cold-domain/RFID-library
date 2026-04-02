package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联服务接口
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 为用户分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds, Long assignerId);

    /**
     * 移除用户角色
     */
    void removeUserRole(Long userId, Long roleId);
}
