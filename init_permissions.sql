-- ============================================
-- 权限初始化数据（在已有数据库上执行）
-- 执行前请确认 permissions 表为空
-- ============================================

-- 清空现有权限数据（如果有的话）
DELETE FROM role_permissions;
DELETE FROM permissions;

-- 重置自增ID
ALTER TABLE permissions AUTO_INCREMENT = 1;

-- 1. 顶级菜单权限（ID: 1-13）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, sort_order) VALUES
('控制台', 'dashboard', 'menu', 0, '/dashboard', 1),
('公开图书搜索', 'public:book', 'menu', 0, '/public/books', 2),
('读者中心', 'reader', 'menu', 0, NULL, 3),
('图书管理', 'book', 'menu', 0, NULL, 4),
('分类管理', 'category', 'menu', 0, NULL, 5),
('借阅管理', 'borrow', 'menu', 0, NULL, 6),
('预约管理', 'reservation', 'menu', 0, NULL, 7),
('罚款管理', 'fine', 'menu', 0, NULL, 8),
('用户管理', 'user', 'menu', 0, NULL, 9),
('角色管理', 'role', 'menu', 0, NULL, 10),
('权限管理', 'permission', 'menu', 0, NULL, 11),
('审计日志', 'audit', 'menu', 0, NULL, 12),
('异常管理', 'exception', 'menu', 0, NULL, 13);

-- 2. 读者中心子权限（父ID=3）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, sort_order) VALUES
('个人资料', 'reader:profile', 'menu', 3, '/reader/profile', 1),
('我的借阅', 'reader:borrows', 'menu', 3, '/reader/borrows', 2),
('我的预约', 'reader:reservations', 'menu', 3, '/reader/reservations', 3),
('我的罚款', 'reader:fines', 'menu', 3, '/reader/fines', 4);

-- 3. 图书管理子权限（父ID=4）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('图书查看', 'book:view', 'button', 4, '/librarian/books', 'GET', 1),
('图书新增', 'book:create', 'button', 4, '/librarian/books', 'POST', 2),
('图书编辑', 'book:update', 'button', 4, '/librarian/books', 'PUT', 3),
('图书删除', 'book:delete', 'button', 4, '/librarian/books', 'DELETE', 4);

-- 4. 分类管理子权限（父ID=5）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('分类查看', 'category:view', 'button', 5, '/librarian/categories', 'GET', 1),
('分类新增', 'category:create', 'button', 5, '/librarian/categories', 'POST', 2),
('分类编辑', 'category:update', 'button', 5, '/librarian/categories', 'PUT', 3),
('分类删除', 'category:delete', 'button', 5, '/librarian/categories', 'DELETE', 4);

-- 5. 借阅管理子权限（父ID=6）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('借阅查看', 'borrow:view', 'button', 6, '/librarian/borrows', 'GET', 1),
('借阅操作', 'borrow:operate', 'button', 6, '/librarian/borrows', 'POST', 2);

-- 6. 预约管理子权限（父ID=7）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('预约查看', 'reservation:view', 'button', 7, '/librarian/reservations', 'GET', 1),
('预约处理', 'reservation:operate', 'button', 7, '/librarian/reservations', 'PUT', 2);

-- 7. 罚款管理子权限（父ID=8）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('罚款查看', 'fine:view', 'button', 8, '/librarian/fines', 'GET', 1),
('罚款操作', 'fine:operate', 'button', 8, '/librarian/fines', 'POST', 2);

-- 8. 用户管理子权限（父ID=9）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('用户查看', 'user:view', 'button', 9, '/admin/users', 'GET', 1),
('用户新增', 'user:create', 'button', 9, '/admin/users', 'POST', 2),
('用户编辑', 'user:update', 'button', 9, '/admin/users', 'PUT', 3),
('用户删除', 'user:delete', 'button', 9, '/admin/users', 'DELETE', 4);

-- 9. 角色管理子权限（父ID=10）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('角色查看', 'role:view', 'button', 10, '/admin/roles', 'GET', 1),
('角色新增', 'role:create', 'button', 10, '/admin/roles', 'POST', 2),
('角色编辑', 'role:update', 'button', 10, '/admin/roles', 'PUT', 3),
('角色删除', 'role:delete', 'button', 10, '/admin/roles', 'DELETE', 4);

-- 10. 权限管理子权限（父ID=11）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('权限查看', 'permission:view', 'button', 11, '/admin/permissions', 'GET', 1),
('权限新增', 'permission:create', 'button', 11, '/admin/permissions', 'POST', 2),
('权限编辑', 'permission:update', 'button', 11, '/admin/permissions', 'PUT', 3),
('权限删除', 'permission:delete', 'button', 11, '/admin/permissions', 'DELETE', 4);

-- 11. 审计日志子权限（父ID=12）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('日志查看', 'audit:view', 'button', 12, '/admin/audit-logs', 'GET', 1),
('日志清理', 'audit:clean', 'button', 12, '/admin/audit-logs', 'DELETE', 2);

-- 12. 异常管理子权限（父ID=13）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('异常查看', 'exception:view', 'button', 13, '/admin/exceptions', 'GET', 1),
('异常处理', 'exception:resolve', 'button', 13, '/admin/exceptions', 'POST', 2);
