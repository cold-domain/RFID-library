package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.service.RoleService;
import com.library.service.UserRoleService;
import com.library.service.UserService;
import com.library.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询用户列表（返回UserVO，含角色信息）
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<UserVO> page = userService.getUserPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 根据ID查询用户详情（返回UserVO）
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 查询用户角色
        List<Role> roles = roleService.getRolesByUserId(id);
        UserVO vo = new UserVO();
        vo.setId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getAccountStatus());
        vo.setRoles(roles.stream().map(Role::getRoleName).collect(Collectors.toList()));
        vo.setRoleIds(roles.stream().map(Role::getRoleId).collect(Collectors.toList()));
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().format(FMT));
        }
        return Result.success(vo);
    }

    /**
     * 新增用户（自动分配读者角色）
     */
    @PostMapping
    public Result<?> add(@RequestBody User user) {
        User registered = userService.register(user);
        // 自动分配读者角色（与公开注册保持一致）
        com.library.entity.Role readerRole = roleService.getByRoleCode("reader");
        if (readerRole != null) {
            com.library.entity.UserRole userRole = new com.library.entity.UserRole();
            userRole.setUserId(registered.getUserId());
            userRole.setRoleId(readerRole.getRoleId());
            userRole.setStatus(1);
            userRoleService.save(userRole);
        }
        return Result.success("新增用户成功");
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody User user) {
        user.setUserId(id);
        user.setPassword(null); // 不通过此接口修改密码
        userService.updateById(user);
        return Result.success("修改用户信息成功");
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除用户成功");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置为123456");
    }

    /**
     * 更新用户账号状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateAccountStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 为用户分配角色
     */
    @PutMapping("/{id}/roles")
    public Result<?> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        Long assignerId = ContextHolder.getCurrentUserId();
        userRoleService.assignRoles(id, roleIds, assignerId);
        return Result.success("角色分配成功");
    }
}
