package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.RolePermission;

import java.util.List;

/**
 * 角色权限关联服务接口
 */
public interface RolePermissionService extends IService<RolePermission> {

    /**
     * 为角色分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds, Long assignerId);

    /**
     * 移除角色权限
     */
    void removeRolePermission(Long roleId, Long permissionId);
}
