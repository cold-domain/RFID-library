package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);

    /**
     * 获取权限树形结构
     */
    List<Permission> getPermissionTree();
}
