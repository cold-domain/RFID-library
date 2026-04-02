package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.RolePermission;
import com.library.mapper.RolePermissionMapper;
import com.library.service.RolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色权限关联服务实现类
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds, Long assignerId) {
        // 先删除角色原有权限
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        baseMapper.delete(wrapper);
        // 批量插入新权限
        for (Long permissionId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rp.setAssignerId(assignerId);
            rp.setAssignTime(LocalDateTime.now());
            rp.setStatus(1);
            baseMapper.insert(rp);
        }
    }

    @Override
    public void removeRolePermission(Long roleId, Long permissionId) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        wrapper.eq(RolePermission::getPermissionId, permissionId);
        baseMapper.delete(wrapper);
    }
}
