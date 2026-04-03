package com.library;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.constant.Constants;
import com.library.common.utils.ContextHolder;
import com.library.common.utils.JwtUtils;
import com.library.common.vo.Result;
import com.library.controller.*;
import com.library.dto.UserRoleAssignDTO;
import com.library.entity.*;
import com.library.service.*;
import com.library.vo.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.library.TestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private RoleService roleService;
    @Mock
    private UserRoleService userRoleService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController controller;

    @Captor
    private ArgumentCaptor<UserRole> userRoleCaptor;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void loginReturnsErrorWhenUsernameOrPasswordMissing() {
        Result<?> result = controller.login(Map.of("username", "alice"));

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
        verifyNoInteractions(userService, roleService, userRoleService, permissionService, jwtUtils);
    }

    @Test
    void loginReturnsTokenAndMenuTreeWhenCredentialsValid() {
        User user = user(1L, "alice");
        Permission parentMenu = permission(10L, "System", "menu", null, null, 2);
        Permission childMenu = permission(11L, "Users", "menu", 10L, "/admin/users", 1);
        Permission directMenu = permission(20L, "Dashboard", "menu", null, "/dashboard", 1);
        Permission button = permission(21L, "Hidden", "button", 20L, "/hidden", 2);

        when(userService.login("alice", "secret")).thenReturn(user);
        when(userService.getUserRoleCodes(1L)).thenReturn(List.of("admin"));
        when(jwtUtils.generateToken("alice", "admin")).thenReturn("jwt-token");
        when(userService.getUserPermissionCodes(1L)).thenReturn(List.of("dashboard:view", "user:view"));
        when(permissionService.getPermissionsByUserId(1L)).thenReturn(List.of(parentMenu, childMenu, directMenu, button));

        Result<?> result = controller.login(Map.of("username", "alice", "password", "secret"));

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        Map<String, Object> data = castMap(result.getData());
        assertThat(data.get("token")).isEqualTo("jwt-token");
        assertThat(data.get("userId")).isEqualTo(1L);
        assertThat(data.get("roles")).isEqualTo(List.of("admin"));
        assertThat(data.get("permissionCodes")).isEqualTo(List.of("dashboard:view", "user:view"));

        @SuppressWarnings("unchecked")
        List<PermissionVO> menus = (List<PermissionVO>) data.get("menus");
        assertThat(menus).hasSize(2);
        assertThat(menus.get(0).getId()).isEqualTo(20L);
        assertThat(menus.get(0).getUrl()).isEqualTo("/dashboard");
        assertThat(menus.get(1).getId()).isEqualTo(10L);
        assertThat(menus.get(1).getUrl()).isEqualTo("/admin/users");
        assertThat(menus.get(1).getChildren()).extracting(PermissionVO::getId).containsExactly(11L);

        verify(userService).updateLoginInfo(1L);
    }

    @Test
    void meReturnsUnauthorizedWhenCurrentUserMissing() {
        Result<?> result = controller.me();

        assertThat(result.getCode()).isEqualTo(Constants.UNAUTHORIZED);
        verifyNoInteractions(userService, permissionService);
    }

    @Test
    void meReturnsCurrentUserInfo() {
        User currentUser = user(2L, "bob");
        currentUser.setRealName("Bob");
        ContextHolder.setCurrentUser(currentUser);

        when(userService.getUserRoleCodes(2L)).thenReturn(List.of("reader"));
        when(userService.getUserPermissionCodes(2L)).thenReturn(List.of("reader:profile"));
        when(permissionService.getPermissionsByUserId(2L)).thenReturn(List.of(permission(100L, "Profile", "menu", null, "/reader/profile", 1)));

        Result<?> result = controller.me();

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        Map<String, Object> data = castMap(result.getData());
        assertThat(data.get("username")).isEqualTo("bob");
        assertThat(data.get("roles")).isEqualTo(List.of("reader"));
        assertThat(data.get("permissionCodes")).isEqualTo(List.of("reader:profile"));
    }

    @Test
    void registerAssignsReaderRoleWhenRoleExists() {
        User newUser = user(null, "new-user");
        User registered = user(8L, "new-user");
        Role readerRole = role(6L, "reader", "Reader");

        when(userService.register(newUser)).thenReturn(registered);
        when(roleService.getByRoleCode("reader")).thenReturn(readerRole);

        Result<?> result = controller.register(newUser);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userRoleService).save(userRoleCaptor.capture());
        UserRole saved = userRoleCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(8L);
        assertThat(saved.getRoleId()).isEqualTo(6L);
        assertThat(saved.getStatus()).isEqualTo(1);
    }

    @Test
    void registerSkipsRoleAssignmentWhenReaderRoleMissing() {
        User newUser = user(null, "new-user");
        User registered = user(8L, "new-user");

        when(userService.register(newUser)).thenReturn(registered);
        when(roleService.getByRoleCode("reader")).thenReturn(null);

        Result<?> result = controller.register(newUser);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userRoleService, never()).save(any(UserRole.class));
    }
}

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRoleService userRoleService;
    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserController controller;

    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<UserRole> userRoleCaptor;
    @Captor
    private ArgumentCaptor<List<UserRoleAssignDTO>> roleAssignmentsCaptor;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void listReturnsPagedUsers() {
        Page<UserVO> page = pageOf(userVo(1L, "alice"));
        when(userService.getUserPage(1, 10, "al")).thenReturn(page);

        Result<?> result = controller.list(1, 10, "al");

        assertThat(result.getData()).isSameAs(page);
    }

    @Test
    void getByIdReturnsErrorWhenUserMissing() {
        when(userService.getById(5L)).thenReturn(null);

        Result<?> result = controller.getById(5L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void getByIdBuildsUserVoWithRoleInfo() {
        User user = user(5L, "alice");
        user.setRealName("Alice");
        user.setEmail("a@example.com");
        user.setPhone("13800000000");
        user.setAccountStatus(1);
        user.setCreateTime(LocalDateTime.of(2026, 4, 1, 12, 30));
        Role admin = role(1L, "admin", "Admin");
        Role reader = role(2L, "reader", "Reader");

        when(userService.getById(5L)).thenReturn(user);
        when(roleService.getRolesByUserId(5L)).thenReturn(List.of(admin, reader));

        Result<?> result = controller.getById(5L);

        UserVO vo = (UserVO) result.getData();
        assertThat(vo.getId()).isEqualTo(5L);
        assertThat(vo.getRoles()).containsExactly("Admin", "Reader");
        assertThat(vo.getRoleIds()).containsExactly(1L, 2L);
        assertThat(vo.getCreateTime()).isEqualTo("2026-04-01 12:30:00");
    }

    @Test
    void addRegistersUserAndAssignsReaderRoleWhenPresent() {
        User request = user(null, "alice");
        User registered = user(3L, "alice");
        Role readerRole = role(9L, "reader", "Reader");
        when(userService.register(request)).thenReturn(registered);
        when(roleService.getByRoleCode("reader")).thenReturn(readerRole);

        Result<?> result = controller.add(request);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userRoleService).save(userRoleCaptor.capture());
        assertThat(userRoleCaptor.getValue().getUserId()).isEqualTo(3L);
        assertThat(userRoleCaptor.getValue().getRoleId()).isEqualTo(9L);
    }

    @Test
    void updateSetsIdAndClearsPasswordBeforeSaving() {
        User payload = user(null, "alice");
        payload.setPassword("secret");

        Result<?> result = controller.update(7L, payload);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userService).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(7L);
        assertThat(userCaptor.getValue().getPassword()).isNull();
    }

    @Test
    void deleteDelegatesToService() {
        Result<?> result = controller.delete(4L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userService).removeById(4L);
    }

    @Test
    void resetPasswordDelegatesToService() {
        Result<?> result = controller.resetPassword(4L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userService).resetPassword(4L);
    }

    @Test
    void updateStatusDelegatesToService() {
        Result<?> result = controller.updateStatus(4L, 0);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userService).updateAccountStatus(4L, 0);
    }

    @Test
    void assignRolesUsesCurrentUserAndEmptyListWhenBodyMissing() {
        ContextHolder.setCurrentUser(user(99L, "operator"));

        Result<?> result = controller.assignRoles(8L, null);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userRoleService).assignRoles(eq(8L), roleAssignmentsCaptor.capture(), eq(99L));
        assertThat(roleAssignmentsCaptor.getValue()).isEmpty();
    }
}

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;
    @Mock
    private RolePermissionService rolePermissionService;
    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private RoleController controller;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void listMapsRolesToVo() {
        Role role = role(1L, "admin", "Admin");
        role.setDescription("desc");
        role.setStatus(1);
        role.setIsSystemRole(0);
        role.setSortOrder(3);
        role.setCreateTime(LocalDateTime.of(2026, 4, 1, 9, 0));
        when(roleService.list()).thenReturn(List.of(role));

        Result<?> result = controller.list();

        @SuppressWarnings("unchecked")
        List<RoleVO> roles = (List<RoleVO>) result.getData();
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRoleCode()).isEqualTo("admin");
        assertThat(roles.get(0).getCreateTime()).isEqualTo("2026-04-01 09:00:00");
    }

    @Test
    void getByIdReturnsErrorWhenMissing() {
        when(roleService.getById(1L)).thenReturn(null);

        Result<?> result = controller.getById(1L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void addSetsCreatorIdFromContext() {
        ContextHolder.setCurrentUser(user(12L, "operator"));
        Role payload = role(null, "editor", "Editor");

        Result<?> result = controller.add(payload);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(roleService).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getCreatorId()).isEqualTo(12L);
    }

    @Test
    void updateSetsRoleIdBeforeSaving() {
        Role payload = role(null, "editor", "Editor");

        Result<?> result = controller.update(6L, payload);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(roleService).updateById(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleId()).isEqualTo(6L);
    }

    @Test
    void deleteRejectsSystemRole() {
        Role role = role(1L, "system", "System");
        role.setIsSystemRole(1);
        when(roleService.getById(1L)).thenReturn(role);

        Result<?> result = controller.delete(1L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
        verify(roleService, never()).removeById(anyLong());
    }

    @Test
    void deleteRemovesAssociationsAndRoleForNormalRole() {
        Role role = role(2L, "editor", "Editor");
        role.setIsSystemRole(0);
        when(roleService.getById(2L)).thenReturn(role);

        Result<?> result = controller.delete(2L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userRoleService).remove(any());
        verify(rolePermissionService).remove(any());
        verify(roleService).removeById(2L);
    }

    @Test
    void assignPermissionsUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(18L, "operator"));

        Result<?> result = controller.assignPermissions(3L, List.of(7L, 8L));

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(rolePermissionService).assignPermissions(3L, List.of(7L, 8L), 18L);
    }
}

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController controller;

    @Captor
    private ArgumentCaptor<Permission> permissionCaptor;

    @Test
    void treeBuildsPermissionHierarchy() {
        Permission root = permission(1L, "Root", "menu", null, "/root", 1);
        Permission child = permission(2L, "Child", "menu", 1L, "/child", 2);
        when(permissionService.list()).thenReturn(List.of(root, child));

        Result<?> result = controller.tree();

        @SuppressWarnings("unchecked")
        List<PermissionVO> tree = (List<PermissionVO>) result.getData();
        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getId()).isEqualTo(2L);
    }

    @Test
    void listReturnsPermissions() {
        List<Permission> permissions = List.of(permission(1L, "Root", "menu", null, "/root", 1));
        when(permissionService.list()).thenReturn(permissions);

        Result<?> result = controller.list();

        assertThat(result.getData()).isEqualTo(permissions);
    }

    @Test
    void getByRoleIdMapsPermissionsToTreeNodes() {
        when(permissionService.getPermissionsByRoleId(4L)).thenReturn(List.of(permission(8L, "Manage", "menu", null, null, 1)));

        Result<?> result = controller.getByRoleId(4L);

        @SuppressWarnings("unchecked")
        List<PermissionTreeNode> nodes = (List<PermissionTreeNode>) result.getData();
        assertThat(nodes).extracting(PermissionTreeNode::getId).containsExactly(8L);
        assertThat(nodes).extracting(PermissionTreeNode::getName).containsExactly("Manage");
    }

    @Test
    void addConvertsVoAndSetsEnabledStatus() {
        PermissionVO vo = new PermissionVO();
        vo.setName("User");
        vo.setPermissionCode("user:view");
        vo.setType("menu");
        vo.setParentId(1L);
        vo.setUrl("/users");
        vo.setSort(3);

        Result<?> result = controller.add(vo);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(permissionService).save(permissionCaptor.capture());
        Permission saved = permissionCaptor.getValue();
        assertThat(saved.getPermissionName()).isEqualTo("User");
        assertThat(saved.getStatus()).isEqualTo(1);
    }

    @Test
    void updateConvertsVoAndSetsId() {
        PermissionVO vo = new PermissionVO();
        vo.setName("User");
        vo.setPermissionCode("user:update");
        vo.setType("menu");

        Result<?> result = controller.update(5L, vo);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(permissionService).updateById(permissionCaptor.capture());
        assertThat(permissionCaptor.getValue().getPermissionId()).isEqualTo(5L);
    }

    @Test
    void deleteDelegatesToService() {
        Result<?> result = controller.delete(9L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(permissionService).removeById(9L);
    }
}

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController controller;

    @Test
    void listConvertsAuditLogsToVo() {
        AuditLog log = new AuditLog();
        log.setAuditLogId(1L);
        log.setUsername("admin");
        log.setOperationType("login");
        log.setLogLevel("INFO");
        log.setOperationDesc("login success");
        log.setCreateTime(LocalDateTime.of(2026, 4, 1, 10, 0));
        Page<AuditLog> page = pageOf(log);
        when(auditLogService.getAuditLogPage(1, 10, "login", "INFO", 1L)).thenReturn(page);

        Result<?> result = controller.list(1, 10, "login", "INFO", 1L);

        @SuppressWarnings("unchecked")
        Page<AuditLogVO> voPage = (Page<AuditLogVO>) result.getData();
        assertThat(voPage.getRecords()).hasSize(1);
        assertThat(voPage.getRecords().get(0).getId()).isEqualTo(1L);
        assertThat(voPage.getRecords().get(0).getCreateTime()).isEqualTo("2026-04-01 10:00:00");
    }

    @Test
    void overviewReturnsSummary() {
        AuditLogOverviewVO overview = new AuditLogOverviewVO();
        when(auditLogService.getOverview()).thenReturn(overview);

        Result<?> result = controller.overview();

        assertThat(result.getData()).isSameAs(overview);
    }

    @Test
    void getByIdReturnsErrorWhenMissing() {
        when(auditLogService.getById(2L)).thenReturn(null);

        Result<?> result = controller.getById(2L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void cleanUsesDefaultRetentionWhenMissing() {
        Result<?> result = controller.clean(null);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(auditLogService).cleanExpiredLogs(Constants.LOG_RETENTION_DAYS);
    }
}

@ExtendWith(MockitoExtension.class)
class ExceptionControllerTest {

    @Mock
    private ExceptionRecordService exceptionRecordService;

    @InjectMocks
    private ExceptionController controller;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void listConvertsExceptionRecordsToVo() {
        ExceptionRecord record = new ExceptionRecord();
        record.setExceptionId(1L);
        record.setExceptionType("business");
        record.setExceptionLevel("medium");
        record.setExceptionMessage("bad request");
        record.setCreateTime(LocalDateTime.of(2026, 4, 1, 10, 0));
        Page<ExceptionRecord> page = pageOf(record);
        when(exceptionRecordService.getExceptionPage(1, 10, "business", "medium", 0)).thenReturn(page);

        Result<?> result = controller.list(1, 10, "business", "medium", 0);

        @SuppressWarnings("unchecked")
        Page<ExceptionRecordVO> voPage = (Page<ExceptionRecordVO>) result.getData();
        assertThat(voPage.getRecords()).hasSize(1);
        assertThat(voPage.getRecords().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void overviewReturnsSummary() {
        ExceptionOverviewVO overview = new ExceptionOverviewVO();
        when(exceptionRecordService.getOverview()).thenReturn(overview);

        Result<?> result = controller.overview();

        assertThat(result.getData()).isSameAs(overview);
    }

    @Test
    void getByIdReturnsErrorWhenMissing() {
        when(exceptionRecordService.getById(2L)).thenReturn(null);

        Result<?> result = controller.getById(2L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void resolveUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(88L, "admin"));

        Result<?> result = controller.resolve(3L, "done");

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(exceptionRecordService).resolveException(3L, 88L, "done");
    }

    @Test
    void ignoreUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(88L, "admin"));

        Result<?> result = controller.ignore(3L, "ignored");

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(exceptionRecordService).ignoreException(3L, 88L, "ignored");
    }
}
