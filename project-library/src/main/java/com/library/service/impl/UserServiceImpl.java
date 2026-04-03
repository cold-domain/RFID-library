package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.entity.UserRole;
import com.library.entity.Permission;
import com.library.common.constant.Constants;
import com.library.mapper.PermissionMapper;
import com.library.mapper.RoleMapper;
import com.library.mapper.UserMapper;
import com.library.mapper.UserRoleMapper;
import com.library.service.UserService;
import com.library.vo.UserRoleAssignmentVO;
import com.library.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public User login(String username, String password) {
        User user = baseMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getAccountStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (user.getAccountStatus() == 2) {
            throw new BusinessException("账号已被锁定，请稍后再试");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        User existing = baseMapper.selectByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        // 检查学号是否已存在
        if (StringUtils.hasText(user.getStudentId())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getStudentId, user.getStudentId());
            if (baseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("该学号已被注册");
            }
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountStatus(1);
        user.setCurrentBorrowCount(0);
        user.setMaxBorrowCount(5);
        user.setLoginCount(0);
        baseMapper.insert(user);
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public IPage<UserVO> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword)
                    .or().like(User::getPhone, keyword)
            );
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> userPage = baseMapper.selectPage(page, wrapper);

        // 批量查询角色信息，避免 N+1
        List<User> users = userPage.getRecords();
        Map<Long, List<String>> roleNamesMap = new HashMap<>();
        Map<Long, List<Long>> roleIdsMap = new HashMap<>();
        Map<Long, List<UserRoleAssignmentVO>> roleAssignmentsMap = new HashMap<>();
        if (!users.isEmpty()) {
            List<Long> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
            // 查 user_roles
            LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.in(UserRole::getUserId, userIds)
                    .eq(UserRole::getStatus, 1)
                    .and(w -> w.isNull(UserRole::getExpireDate).or().ge(UserRole::getExpireDate, LocalDate.now()));
            List<UserRole> userRoles = userRoleMapper.selectList(urWrapper);
            // 查 roles
            Set<Long> roleIdSet = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
            Map<Long, Role> roleMap = new HashMap<>();
            if (!roleIdSet.isEmpty()) {
                List<Role> roles = roleMapper.selectBatchIds(roleIdSet);
                roleMap = roles.stream()
                        .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                        .collect(Collectors.toMap(Role::getRoleId, role -> role));
            }
            // 组装每个用户的角色名称和角色ID（过滤已删除的角色）
            for (UserRole ur : userRoles) {
                Long uid = ur.getUserId();
                Long rid = ur.getRoleId();
                Role role = roleMap.get(rid);
                if (role != null) {
                    roleNamesMap.computeIfAbsent(uid, k -> new ArrayList<>()).add(role.getRoleName());
                    roleIdsMap.computeIfAbsent(uid, k -> new ArrayList<>()).add(rid);
                    UserRoleAssignmentVO assignmentVO = new UserRoleAssignmentVO();
                    assignmentVO.setRoleId(rid);
                    assignmentVO.setRoleName(role.getRoleName());
                    assignmentVO.setRoleCode(role.getRoleCode());
                    assignmentVO.setExpireDate(ur.getExpireDate());
                    roleAssignmentsMap.computeIfAbsent(uid, k -> new ArrayList<>()).add(assignmentVO);
                }
            }
        }

        // 转换为 UserVO
        Map<Long, List<String>> finalRoleNamesMap = roleNamesMap;
        Map<Long, List<Long>> finalRoleIdsMap = roleIdsMap;
        Map<Long, List<UserRoleAssignmentVO>> finalRoleAssignmentsMap = roleAssignmentsMap;
        return userPage.convert(user -> toVO(user, finalRoleNamesMap, finalRoleIdsMap, finalRoleAssignmentsMap));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        User update = new User();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        baseMapper.updateById(update);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User update = new User();
        update.setUserId(userId);
        // 默认重置密码为123456
        update.setPassword(passwordEncoder.encode("123456"));
        baseMapper.updateById(update);
    }

    @Override
    public void updateAccountStatus(Long userId, Integer status) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User update = new User();
        update.setUserId(userId);
        update.setAccountStatus(status);
        baseMapper.updateById(update);
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        List<String> roleCodes = getUserRoleCodes(userId);
        if (roleCodes.contains(Constants.ROLE_SYSTEM_ADMIN) || roleCodes.contains(Constants.ROLE_SUPER_ADMIN)) {
            LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Permission::getStatus, 1).orderByAsc(Permission::getSortOrder);
            return permissionMapper.selectList(wrapper).stream()
                    .map(Permission::getPermissionCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return baseMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    public void updateLoginInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user != null) {
            User update = new User();
            update.setUserId(userId);
            update.setLastLoginTime(LocalDateTime.now());
            update.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
            baseMapper.updateById(update);
        }
    }

    private UserVO toVO(User user,
                        Map<Long, List<String>> roleNamesMap,
                        Map<Long, List<Long>> roleIdsMap,
                        Map<Long, List<UserRoleAssignmentVO>> roleAssignmentsMap) {
        UserVO vo = new UserVO();
        vo.setId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getAccountStatus());
        vo.setRoles(roleNamesMap.getOrDefault(user.getUserId(), Collections.emptyList()));
        vo.setRoleIds(roleIdsMap.getOrDefault(user.getUserId(), Collections.emptyList()));
        vo.setRoleAssignments(roleAssignmentsMap.getOrDefault(user.getUserId(), Collections.emptyList()));
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().format(FMT));
        }
        return vo;
    }
}
