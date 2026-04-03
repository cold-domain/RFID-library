package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.UserRoleAssignDTO;
import com.library.entity.UserRole;
import com.library.mapper.UserRoleMapper;
import com.library.service.UserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色关联服务实现类
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    @Transactional
    public void assignRoles(Long userId, List<UserRoleAssignDTO> roleAssignments, Long assignerId) {
        // 先删除用户原有角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        baseMapper.delete(wrapper);
        // 批量插入新角色
        for (UserRoleAssignDTO roleAssignment : roleAssignments) {
            if (roleAssignment == null || roleAssignment.getRoleId() == null) {
                continue;
            }
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleAssignment.getRoleId());
            userRole.setAssignerId(assignerId);
            userRole.setAssignTime(LocalDateTime.now());
            userRole.setExpireDate(roleAssignment.getExpireDate());
            userRole.setStatus(1);
            baseMapper.insert(userRole);
        }
    }

    @Override
    public void removeUserRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getRoleId, roleId);
        baseMapper.delete(wrapper);
    }
}
