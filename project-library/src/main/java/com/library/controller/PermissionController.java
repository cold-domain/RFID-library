package com.library.controller;

import com.library.common.vo.Result;
import com.library.entity.Permission;
import com.library.service.PermissionService;
import com.library.vo.PermissionTreeNode;
import com.library.vo.PermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 查询权限树（返回完整 VO 树形结构，供权限管理页面和角色分配使用）
     */
    @GetMapping("/tree")
    public Result<?> tree() {
        List<Permission> allPermissions = permissionService.list();
        List<PermissionVO> tree = buildTree(allPermissions);
        return Result.success(tree);
    }

    /**
     * 查询所有权限（扁平列表）
     */
    @GetMapping
    public Result<?> list() {
        List<Permission> permissions = permissionService.list();
        return Result.success(permissions);
    }

    /**
     * 根据角色ID查询权限（返回 id/name 结构，供角色权限分配回显）
     */
    @GetMapping("/role/{roleId}")
    public Result<?> getByRoleId(@PathVariable Long roleId) {
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        List<PermissionTreeNode> result = permissions.stream().map(p -> {
            PermissionTreeNode node = new PermissionTreeNode();
            node.setId(p.getPermissionId());
            node.setName(p.getPermissionName());
            return node;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 新增权限
     */
    @PostMapping
    public Result<?> add(@RequestBody PermissionVO vo) {
        Permission permission = voToEntity(vo);
        permission.setStatus(1);
        permissionService.save(permission);
        return Result.success("新增权限成功");
    }

    /**
     * 修改权限
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PermissionVO vo) {
        Permission permission = voToEntity(vo);
        permission.setPermissionId(id);
        permissionService.updateById(permission);
        return Result.success("修改权限成功");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        permissionService.removeById(id);
        return Result.success("删除权限成功");
    }

    /**
     * VO → Entity 转换
     */
    private Permission voToEntity(PermissionVO vo) {
        Permission p = new Permission();
        p.setPermissionName(vo.getName());
        p.setPermissionCode(vo.getPermissionCode());
        p.setPermissionType(vo.getType());
        p.setParentPermissionId(vo.getParentId());
        p.setUrlPath(vo.getUrl());
        p.setSortOrder(vo.getSort());
        if (vo.getStatus() != null) {
            p.setStatus(vo.getStatus());
        }
        return p;
    }

    /**
     * 构建权限树（两次遍历：先建节点Map，再组装父子关系）
     */
    private List<PermissionVO> buildTree(List<Permission> allPermissions) {
        Map<Long, PermissionVO> nodeMap = new LinkedHashMap<>();
        for (Permission p : allPermissions) {
            PermissionVO vo = new PermissionVO();
            vo.setId(p.getPermissionId());
            vo.setName(p.getPermissionName());
            vo.setPermissionCode(p.getPermissionCode());
            vo.setType(p.getPermissionType());
            vo.setParentId(p.getParentPermissionId());
            vo.setUrl(p.getUrlPath());
            vo.setSort(p.getSortOrder());
            vo.setStatus(p.getStatus());
            vo.setChildren(new ArrayList<>());
            nodeMap.put(p.getPermissionId(), vo);
        }

        List<PermissionVO> roots = new ArrayList<>();
        for (Permission p : allPermissions) {
            PermissionVO node = nodeMap.get(p.getPermissionId());
            if (p.getParentPermissionId() == null || p.getParentPermissionId() == 0) {
                roots.add(node);
            } else {
                PermissionVO parent = nodeMap.get(p.getParentPermissionId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }
}
