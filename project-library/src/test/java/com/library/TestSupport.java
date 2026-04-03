package com.library;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Permission;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.vo.CategoryVO;
import com.library.vo.UserVO;

import java.util.Arrays;
import java.util.Map;

final class TestSupport {

    private TestSupport() {
    }

    static User user(Long id, String username) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        return user;
    }

    static Role role(Long id, String code, String name) {
        Role role = new Role();
        role.setRoleId(id);
        role.setRoleCode(code);
        role.setRoleName(name);
        return role;
    }

    static Permission permission(Long id, String name, String type, Long parentId, String url, Integer sort) {
        Permission permission = new Permission();
        permission.setPermissionId(id);
        permission.setPermissionName(name);
        permission.setPermissionType(type);
        permission.setParentPermissionId(parentId);
        permission.setUrlPath(url);
        permission.setSortOrder(sort);
        permission.setStatus(1);
        return permission;
    }

    static CategoryVO categoryVo(Long id, String name, Long parentId) {
        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setId(id);
        categoryVO.setName(name);
        categoryVO.setParentId(parentId);
        return categoryVO;
    }

    static UserVO userVo(Long id, String username) {
        UserVO userVO = new UserVO();
        userVO.setId(id);
        userVO.setUsername(username);
        return userVO;
    }

    @SafeVarargs
    static <T> Page<T> pageOf(T... records) {
        Page<T> page = new Page<>(1, Math.max(records.length, 1));
        page.setRecords(Arrays.asList(records));
        page.setTotal(records.length);
        return page;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
