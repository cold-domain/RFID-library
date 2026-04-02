package com.library.common.utils;

import com.library.entity.User;

/**
 * 上下文持有者，用于存储当前登录用户信息
 */
public class ContextHolder {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        currentUser.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}