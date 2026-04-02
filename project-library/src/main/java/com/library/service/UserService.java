package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.User;
import com.library.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 用户注册
     */
    User register(User user);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 分页查询用户列表（返回UserVO，含角色信息）
     */
    IPage<UserVO> getUserPage(int pageNum, int pageSize, String keyword);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(Long userId);

    /**
     * 更新账号状态
     */
    void updateAccountStatus(Long userId, Integer status);

    /**
     * 获取用户角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);

    /**
     * 获取用户权限编码列表
     */
    List<String> getUserPermissionCodes(Long userId);

    /**
     * 记录登录信息
     */
    void updateLoginInfo(Long userId);
}
