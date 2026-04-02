package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 根据角色编码查询角色
     */
    Role getByRoleCode(String roleCode);

    /**
     * 查询所有启用的角色列表
     */
    List<Role> getEnabledRoles();
}
