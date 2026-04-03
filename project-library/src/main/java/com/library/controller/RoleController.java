package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.Role;
import com.library.entity.RolePermission;
import com.library.entity.UserRole;
import com.library.service.RolePermissionService;
import com.library.service.RoleService;
import com.library.service.UserRoleService;
import com.library.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private UserRoleService userRoleService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询所有角色（含禁用）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:view')")
    public Result<?> list() {
        List<Role> roles = roleService.list();
        List<RoleVO> voList = roles.stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:view')")
    public Result<?> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(toVO(role));
    }

    /**
     * 新增角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public Result<?> add(@RequestBody Role role) {
        role.setCreatorId(ContextHolder.getCurrentUserId());
        roleService.save(role);
        return Result.success("新增角色成功");
    }

    /**
     * 修改角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public Result<?> update(@PathVariable Long id, @RequestBody Role role) {
        role.setRoleId(id);
        roleService.updateById(role);
        return Result.success("修改角色成功");
    }

    /**
     * 删除角色（同时清理关联的用户角色和角色权限记录）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<?> delete(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role != null && role.getIsSystemRole() == 1) {
            return Result.error("系统内置角色不允许删除");
        }
        // 清理 user_roles 关联记录
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        // 清理 role_permissions 关联记录
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        // 删除角色
        roleService.removeById(id);
        return Result.success("删除角色成功");
    }

    /**
     * 为角色分配权限
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:update')")
    public Result<?> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        Long assignerId = ContextHolder.getCurrentUserId();
        rolePermissionService.assignPermissions(id, permissionIds, assignerId);
        return Result.success("权限分配成功");
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getRoleId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setIsSystemRole(role.getIsSystemRole());
        vo.setSortOrder(role.getSortOrder());
        if (role.getCreateTime() != null) {
            vo.setCreateTime(role.getCreateTime().format(FMT));
        }
        return vo;
    }
}
