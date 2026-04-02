package com.library.vo;

import lombok.Data;

import java.util.List;

/**
 * 权限树节点（供前端 el-tree 使用）
 */
@Data
public class PermissionTreeNode {

    private Long id;

    private String name;

    private List<PermissionTreeNode> children;
}
