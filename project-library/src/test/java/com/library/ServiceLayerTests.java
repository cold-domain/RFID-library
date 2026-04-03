package com.library;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.constant.Constants;
import com.library.common.exception.BusinessException;
import com.library.dto.CategoryDTO;
import com.library.dto.UserRoleAssignDTO;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.impl.*;
import com.library.vo.CategoryVO;
import com.library.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.library.TestSupport.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private PermissionMapper permissionMapper;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserServiceImpl service;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        ReflectionTestUtils.setField(service, "roleMapper", roleMapper);
        ReflectionTestUtils.setField(service, "userRoleMapper", userRoleMapper);
        ReflectionTestUtils.setField(service, "permissionMapper", permissionMapper);
    }

    @Test
    void loginRejectsMissingDisabledLockedAndWrongPasswordUsers() {
        when(userMapper.selectByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.login("missing", "x"))
                .isInstanceOf(BusinessException.class);

        User disabled = user(1L, "disabled");
        disabled.setAccountStatus(0);
        when(userMapper.selectByUsername("disabled")).thenReturn(disabled);
        assertThatThrownBy(() -> service.login("disabled", "x"))
                .isInstanceOf(BusinessException.class);

        User locked = user(2L, "locked");
        locked.setAccountStatus(2);
        when(userMapper.selectByUsername("locked")).thenReturn(locked);
        assertThatThrownBy(() -> service.login("locked", "x"))
                .isInstanceOf(BusinessException.class);

        User wrongPassword = user(3L, "wrong");
        wrongPassword.setAccountStatus(1);
        wrongPassword.setPassword(passwordEncoder.encode("right"));
        when(userMapper.selectByUsername("wrong")).thenReturn(wrongPassword);
        assertThatThrownBy(() -> service.login("wrong", "bad"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        User valid = user(3L, "valid");
        valid.setAccountStatus(1);
        valid.setPassword(passwordEncoder.encode("right"));
        when(userMapper.selectByUsername("valid")).thenReturn(valid);

        User result = service.login("valid", "right");

        assertThat(result).isSameAs(valid);
    }

    @Test
    void registerValidatesDuplicatesAndSetsDefaults() {
        User existing = user(1L, "existing");
        when(userMapper.selectByUsername("existing")).thenReturn(existing);

        User duplicate = user(null, "existing");
        assertThatThrownBy(() -> service.register(duplicate))
                .isInstanceOf(BusinessException.class);

        User studentDuplicate = user(null, "alice");
        studentDuplicate.setStudentId("S001");
        when(userMapper.selectByUsername("alice")).thenReturn(null);
        when(userMapper.selectCount(any())).thenReturn(1L);
        assertThatThrownBy(() -> service.register(studentDuplicate))
                .isInstanceOf(BusinessException.class);

        when(userMapper.selectCount(any())).thenReturn(0L);
        User fresh = user(null, "fresh");
        fresh.setPassword("secret");
        fresh.setStudentId("S002");

        User result = service.register(fresh);

        assertThat(result).isSameAs(fresh);
        verify(userMapper).insert(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getAccountStatus()).isEqualTo(1);
        assertThat(saved.getCurrentBorrowCount()).isEqualTo(0);
        assertThat(saved.getMaxBorrowCount()).isEqualTo(5);
        assertThat(saved.getLoginCount()).isEqualTo(0);
        assertThat(passwordEncoder.matches("secret", saved.getPassword())).isTrue();
    }

    @Test
    void getByUsernameDelegatesToMapper() {
        User user = user(1L, "alice");
        when(userMapper.selectByUsername("alice")).thenReturn(user);

        assertThat(service.getByUsername("alice")).isSameAs(user);
    }

    @Test
    void getUserPageEnrichesRoleNamesIdsAndAssignments() {
        User user = user(1L, "alice");
        user.setRealName("Alice");
        user.setAccountStatus(1);
        user.setCreateTime(LocalDateTime.of(2026, 4, 1, 10, 0));
        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(user));

        UserRole userRole = new UserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(7L);
        userRole.setStatus(1);
        userRole.setExpireDate(LocalDate.now().plusDays(1));

        Role role = new Role();
        role.setRoleId(7L);
        role.setRoleName("Admin");
        role.setRoleCode("admin");
        role.setStatus(1);

        when(userMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectBatchIds(any(Collection.class))).thenReturn(List.of(role));

        IPage<UserVO> result = service.getUserPage(1, 10, "ali");

        assertThat(result.getRecords()).hasSize(1);
        UserVO vo = result.getRecords().get(0);
        assertThat(vo.getRoles()).containsExactly("Admin");
        assertThat(vo.getRoleIds()).containsExactly(7L);
        assertThat(vo.getRoleAssignments()).hasSize(1);
        assertThat(vo.getRoleAssignments().get(0).getRoleCode()).isEqualTo("admin");
        assertThat(vo.getCreateTime()).isEqualTo("2026-04-01 10:00:00");
    }

    @Test
    void changePasswordValidatesUserAndCurrentPassword() {
        when(userMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.changePassword(1L, "old", "new"))
                .isInstanceOf(BusinessException.class);

        User user = user(2L, "alice");
        user.setPassword(passwordEncoder.encode("correct"));
        when(userMapper.selectById(2L)).thenReturn(user);

        assertThatThrownBy(() -> service.changePassword(2L, "wrong", "new"))
                .isInstanceOf(BusinessException.class);

        service.changePassword(2L, "correct", "new");

        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(2L);
        assertThat(passwordEncoder.matches("new", userCaptor.getValue().getPassword())).isTrue();
    }

    @Test
    void resetPasswordAndUpdateAccountStatusRequireExistingUser() {
        when(userMapper.selectById(1L)).thenReturn(null);
        assertThatThrownBy(() -> service.resetPassword(1L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.updateAccountStatus(1L, 0)).isInstanceOf(BusinessException.class);

        User user = user(2L, "alice");
        when(userMapper.selectById(2L)).thenReturn(user);

        service.resetPassword(2L);
        verify(userMapper).updateById(userCaptor.capture());
        assertThat(passwordEncoder.matches("123456", userCaptor.getValue().getPassword())).isTrue();

        service.updateAccountStatus(2L, 0);
        verify(userMapper, times(2)).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getAccountStatus()).isEqualTo(0);
    }

    @Test
    void roleAndPermissionCodeQueriesUseAdminShortcut() {
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of(Constants.ROLE_SYSTEM_ADMIN));
        Permission permission = new Permission();
        permission.setPermissionCode("user:view");
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        assertThat(service.getUserRoleCodes(1L)).containsExactly(Constants.ROLE_SYSTEM_ADMIN);
        assertThat(service.getUserPermissionCodes(1L)).containsExactly("user:view");

        when(userMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("reader"));
        when(userMapper.selectPermissionCodesByUserId(2L)).thenReturn(List.of("reader:profile"));
        assertThat(service.getUserPermissionCodes(2L)).containsExactly("reader:profile");
    }

    @Test
    void updateLoginInfoIncrementsCounterWhenUserExists() {
        User user = user(3L, "alice");
        user.setLoginCount(2);
        when(userMapper.selectById(3L)).thenReturn(user);

        service.updateLoginInfo(3L);

        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getLoginCount()).isEqualTo(3);
        assertThat(userCaptor.getValue().getLastLoginTime()).isNotNull();
    }
}

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private UserMapper userMapper;

    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PermissionServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", permissionMapper);
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
    }

    @Test
    void permissionQueriesDelegateAndUseAdminShortcut() {
        Permission permission = new Permission();
        permission.setPermissionId(1L);
        permission.setParentPermissionId(null);
        when(permissionMapper.selectPermissionsByRoleId(1L)).thenReturn(List.of(permission));
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of(Constants.ROLE_SUPER_ADMIN));
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        assertThat(service.getPermissionsByRoleId(1L)).hasSize(1);
        assertThat(service.getPermissionsByUserId(1L)).hasSize(1);

        when(userMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("reader"));
        when(permissionMapper.selectPermissionsByUserId(2L)).thenReturn(List.of(permission));
        assertThat(service.getPermissionsByUserId(2L)).hasSize(1);
    }

    @Test
    void getPermissionTreeReturnsOnlyRootNodes() {
        Permission root = new Permission();
        root.setPermissionId(1L);
        root.setParentPermissionId(null);
        Permission child = new Permission();
        child.setPermissionId(2L);
        child.setParentPermissionId(1L);
        when(permissionMapper.selectList(any())).thenReturn(List.of(root, child));

        List<Permission> result = service.getPermissionTree();

        assertThat(result).containsExactly(root);
    }
}

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTest {

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Captor
    private ArgumentCaptor<RolePermission> rolePermissionCaptor;

    private RolePermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RolePermissionServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", rolePermissionMapper);
    }

    @Test
    void assignPermissionsDeletesExistingAndInsertsEachPermission() {
        service.assignPermissions(5L, List.of(7L, 8L), 9L);

        verify(rolePermissionMapper).delete(any());
        verify(rolePermissionMapper, times(2)).insert(rolePermissionCaptor.capture());
        assertThat(rolePermissionCaptor.getAllValues()).extracting(RolePermission::getPermissionId).containsExactly(7L, 8L);
        assertThat(rolePermissionCaptor.getAllValues()).extracting(RolePermission::getRoleId).containsOnly(5L);
        assertThat(rolePermissionCaptor.getAllValues()).extracting(RolePermission::getAssignerId).containsOnly(9L);
    }

    @Test
    void removeRolePermissionDeletesByRoleAndPermission() {
        service.removeRolePermission(5L, 7L);

        verify(rolePermissionMapper).delete(any());
    }
}

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @Captor
    private ArgumentCaptor<UserRole> userRoleCaptor;

    private UserRoleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserRoleServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", userRoleMapper);
    }

    @Test
    void assignRolesDeletesExistingAndSkipsInvalidEntries() {
        UserRoleAssignDTO valid = new UserRoleAssignDTO();
        valid.setRoleId(6L);
        valid.setExpireDate(LocalDate.now().plusDays(1));
        UserRoleAssignDTO invalid = new UserRoleAssignDTO();

        service.assignRoles(3L, List.of(null, invalid, valid), 8L);

        verify(userRoleMapper).delete(any());
        verify(userRoleMapper).insert(userRoleCaptor.capture());
        assertThat(userRoleCaptor.getValue().getUserId()).isEqualTo(3L);
        assertThat(userRoleCaptor.getValue().getRoleId()).isEqualTo(6L);
        assertThat(userRoleCaptor.getValue().getAssignerId()).isEqualTo(8L);
        assertThat(userRoleCaptor.getValue().getExpireDate()).isEqualTo(valid.getExpireDate());
    }

    @Test
    void removeUserRoleDeletesByUserAndRole() {
        service.removeUserRole(3L, 6L);

        verify(userRoleMapper).delete(any());
    }
}

@ExtendWith(MockitoExtension.class)
class BookCategoryServiceImplTest {

    @Mock
    private BookCategoryMapper bookCategoryMapper;

    @Captor
    private ArgumentCaptor<BookCategory> categoryCaptor;

    private BookCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BookCategoryServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", bookCategoryMapper);
    }

    @Test
    void getCategoryTreeAndEnabledCategoriesMapEntitiesToVo() {
        BookCategory root = category(1L, "Technology", null, 1, 1);
        BookCategory child = category(2L, "Java", 1L, 2, 1);
        when(bookCategoryMapper.selectList(any())).thenReturn(List.of(root, child), List.of(root));

        List<CategoryVO> tree = service.getCategoryTree();
        List<CategoryVO> enabled = service.getEnabledCategories();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(enabled).extracting(CategoryVO::getName).containsExactly("Technology");
    }

    @Test
    void getByCategoryCodeDelegatesToMapper() {
        BookCategory category = category(1L, "Technology", null, 1, 1);
        when(bookCategoryMapper.selectOne(any())).thenReturn(category);

        assertThat(service.getByCategoryCode("CAT1")).isSameAs(category);
    }

    @Test
    void addCategoryValidatesParentAndSetsDefaults() {
        CategoryDTO childDto = new CategoryDTO();
        childDto.setName("Java");
        childDto.setParentId(99L);
        when(bookCategoryMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.addCategory(childDto, 1L))
                .isInstanceOf(BusinessException.class);

        CategoryDTO rootDto = new CategoryDTO();
        rootDto.setName("Technology");
        service.addCategory(rootDto, 1L);

        verify(bookCategoryMapper).insert(categoryCaptor.capture());
        BookCategory savedRoot = categoryCaptor.getValue();
        assertThat(savedRoot.getCreatorId()).isEqualTo(1L);
        assertThat(savedRoot.getCategoryLevel()).isEqualTo(1);
        assertThat(savedRoot.getStatus()).isEqualTo(1);
        assertThat(savedRoot.getSortOrder()).isEqualTo(0);
        assertThat(savedRoot.getCategoryCode()).startsWith("CAT");

        BookCategory parent = category(5L, "Computer", null, 2, 1);
        when(bookCategoryMapper.selectById(5L)).thenReturn(parent);
        CategoryDTO nestedDto = new CategoryDTO();
        nestedDto.setName("Networks");
        nestedDto.setParentId(5L);
        service.addCategory(nestedDto, 2L);

        verify(bookCategoryMapper, times(2)).insert(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getCategoryLevel()).isEqualTo(3);
    }

    @Test
    void updateCategoryValidatesInputAndPersistsChanges() {
        when(bookCategoryMapper.selectById(1L)).thenReturn(null);
        CategoryDTO missing = new CategoryDTO();
        missing.setName("x");
        assertThatThrownBy(() -> service.updateCategory(1L, missing)).isInstanceOf(BusinessException.class);

        BookCategory existing = category(2L, "Old", null, 1, 1);
        when(bookCategoryMapper.selectById(2L)).thenReturn(existing);
        CategoryDTO selfParent = new CategoryDTO();
        selfParent.setName("Self");
        selfParent.setParentId(2L);
        assertThatThrownBy(() -> service.updateCategory(2L, selfParent)).isInstanceOf(BusinessException.class);

        CategoryDTO badParent = new CategoryDTO();
        badParent.setName("Child");
        badParent.setParentId(99L);
        when(bookCategoryMapper.selectById(99L)).thenReturn(null);
        assertThatThrownBy(() -> service.updateCategory(2L, badParent)).isInstanceOf(BusinessException.class);

        BookCategory parent = category(5L, "Computer", null, 2, 1);
        when(bookCategoryMapper.selectById(5L)).thenReturn(parent);
        CategoryDTO valid = new CategoryDTO();
        valid.setName("Networks");
        valid.setParentId(5L);
        valid.setSort(3);
        valid.setStatus(0);

        service.updateCategory(2L, valid);

        verify(bookCategoryMapper).updateById(categoryCaptor.capture());
        BookCategory updated = categoryCaptor.getValue();
        assertThat(updated.getCategoryId()).isEqualTo(2L);
        assertThat(updated.getParentCategoryId()).isEqualTo(5L);
        assertThat(updated.getCategoryLevel()).isEqualTo(3);
        assertThat(updated.getSortOrder()).isEqualTo(3);
        assertThat(updated.getStatus()).isEqualTo(0);
    }

    private BookCategory category(Long id, String name, Long parentId, Integer level, Integer status) {
        BookCategory category = new BookCategory();
        category.setCategoryId(id);
        category.setCategoryName(name);
        category.setParentCategoryId(parentId);
        category.setCategoryLevel(level);
        category.setStatus(status);
        return category;
    }
}
