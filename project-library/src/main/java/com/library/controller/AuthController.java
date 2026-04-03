package com.library.controller;

import com.library.common.utils.JwtUtils;
import com.library.common.vo.Result;
import com.library.entity.Permission;
import com.library.entity.User;
import com.library.entity.UserRole;
import com.library.service.PermissionService;
import com.library.service.RoleService;
import com.library.service.UserRoleService;
import com.library.service.UserService;
import com.library.vo.PermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器（公开接口）
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        User user = userService.login(username, password);
        // 获取用户角色
        List<String> roleCodes = userService.getUserRoleCodes(user.getUserId());
        String role = roleCodes.isEmpty() ? "reader" : roleCodes.get(0);
        // 生成JWT token
        String token = jwtUtils.generateToken(username, role);
        // 更新登录信息
        userService.updateLoginInfo(user.getUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("roles", roleCodes);
        data.put("permissionCodes", userService.getUserPermissionCodes(user.getUserId()));
        data.put("menus", buildMenuTree(permissionService.getPermissionsByUserId(user.getUserId())));
        return Result.success("登录成功", data);
    }

    /**
     * 获取当前登录用户信息（每次页面刷新时前端调用，确保角色信息最新）
     */
    @GetMapping("/me")
    public Result<?> me() {
        User user = com.library.common.utils.ContextHolder.getCurrentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        List<String> roleCodes = userService.getUserRoleCodes(user.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("roles", roleCodes);
        data.put("permissionCodes", userService.getUserPermissionCodes(user.getUserId()));
        data.put("menus", buildMenuTree(permissionService.getPermissionsByUserId(user.getUserId())));
        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        User registered = userService.register(user);
        // 默认分配读者角色
        com.library.entity.Role readerRole = roleService.getByRoleCode("reader");
        if (readerRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(registered.getUserId());
            userRole.setRoleId(readerRole.getRoleId());
            userRole.setStatus(1);
            userRoleService.save(userRole);
        }
        return Result.success("注册成功");
    }
    private List<PermissionVO> buildMenuTree(List<Permission> permissions) {
        Map<Long, List<Permission>> childrenMap = new HashMap<>();
        for (Permission permission : permissions) {
            if (permission.getParentPermissionId() != null && permission.getParentPermissionId() > 0) {
                childrenMap.computeIfAbsent(permission.getParentPermissionId(), key -> new ArrayList<>()).add(permission);
            }
        }

        Map<Long, PermissionVO> nodeMap = new HashMap<>();
        for (Permission permission : permissions) {
            if (!"menu".equals(permission.getPermissionType())) {
                continue;
            }
            PermissionVO node = new PermissionVO();
            node.setId(permission.getPermissionId());
            node.setName(permission.getPermissionName());
            node.setPermissionCode(permission.getPermissionCode());
            node.setType(permission.getPermissionType());
            node.setParentId(permission.getParentPermissionId());
            node.setUrl(resolveMenuUrl(permission, childrenMap));
            node.setSort(permission.getSortOrder());
            node.setStatus(permission.getStatus());
            node.setChildren(new ArrayList<>());
            nodeMap.put(permission.getPermissionId(), node);
        }

        List<PermissionVO> roots = new ArrayList<>();
        for (PermissionVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }

        roots.sort((a, b) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(), b.getSort() == null ? 0 : b.getSort()));
        roots.forEach(this::sortMenus);
        return roots;
    }

    private String resolveMenuUrl(Permission permission, Map<Long, List<Permission>> childrenMap) {
        if (StringUtils.hasText(permission.getUrlPath())) {
            return permission.getUrlPath();
        }
        return findFirstReachableUrl(permission.getPermissionId(), childrenMap, new HashSet<>());
    }

    private String findFirstReachableUrl(Long permissionId, Map<Long, List<Permission>> childrenMap, HashSet<Long> visited) {
        if (permissionId == null || !visited.add(permissionId)) {
            return null;
        }
        List<Permission> children = childrenMap.get(permissionId);
        if (children == null || children.isEmpty()) {
            return null;
        }
        children.sort(Comparator.comparingInt(item -> item.getSortOrder() == null ? 0 : item.getSortOrder()));
        for (Permission child : children) {
            if (StringUtils.hasText(child.getUrlPath())) {
                return child.getUrlPath();
            }
            String nestedUrl = findFirstReachableUrl(child.getPermissionId(), childrenMap, visited);
            if (StringUtils.hasText(nestedUrl)) {
                return nestedUrl;
            }
        }
        return null;
    }

    private void sortMenus(PermissionVO node) {
        if (node.getChildren() == null) {
            return;
        }
        node.getChildren().sort((a, b) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(), b.getSort() == null ? 0 : b.getSort()));
        node.getChildren().forEach(this::sortMenus);
    }
}
