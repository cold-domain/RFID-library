-- ============================================
-- 图书馆RFID数据安全管理系统 - 数据库建表脚本
-- 数据库名：library_rfid
-- ============================================

CREATE DATABASE IF NOT EXISTS library_rfid DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_rfid;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    student_id VARCHAR(20) UNIQUE COMMENT '学号（读者证号）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_url VARCHAR(255) COMMENT '头像地址',
    reader_type VARCHAR(20) DEFAULT 'student' COMMENT '读者类型：student/teacher/staff/external',
    department VARCHAR(100) COMMENT '院系/部门',
    major VARCHAR(100) COMMENT '专业',
    enrollment_date DATE COMMENT '入学日期',
    card_expire_date DATE COMMENT '借阅证到期日期',
    account_status TINYINT DEFAULT 1 COMMENT '账号状态：0禁用 1正常 2锁定',
    current_borrow_count INT DEFAULT 0 COMMENT '当前借阅数量',
    max_borrow_count INT DEFAULT 5 COMMENT '最大可借数量',
    fine_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '欠款金额',
    last_borrow_time DATETIME COMMENT '最近借阅时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最近登录时间',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    wechat_openid VARCHAR(100) COMMENT '微信OpenID',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_username (username),
    INDEX idx_student_id (student_id),
    INDEX idx_phone (phone),
    INDEX idx_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 角色表
-- ============================================
CREATE TABLE roles (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    parent_role_id BIGINT DEFAULT 0 COMMENT '父角色ID',
    role_level INT DEFAULT 0 COMMENT '角色层级',
    is_system_role TINYINT DEFAULT 0 COMMENT '是否系统内置角色：0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    sort_order INT DEFAULT 0 COMMENT '排序值，越小越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建人ID',
    remark VARCHAR(500) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================
-- 3. 权限表
-- ============================================
CREATE TABLE permissions (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：menu/button/api',
    parent_permission_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    url_path VARCHAR(200) COMMENT '接口路径',
    http_method VARCHAR(10) COMMENT 'HTTP方法：GET/POST/PUT/DELETE',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序值',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================
-- 4. 用户角色关联表
-- ============================================
CREATE TABLE user_roles (
    user_role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    assigner_id BIGINT COMMENT '分配人ID',
    expire_date DATE COMMENT '过期日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    UNIQUE INDEX uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================
-- 5. 角色权限关联表
-- ============================================
CREATE TABLE role_permissions (
    role_permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    assigner_id BIGINT COMMENT '分配人ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    UNIQUE INDEX uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================
-- 6. 图书表
-- ============================================
CREATE TABLE books (
    book_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图书ID',
    book_name VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    isbn VARCHAR(20) COMMENT 'ISBN号',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date DATE COMMENT '出版日期',
    page_count INT COMMENT '页数',
    price DECIMAL(10,2) COMMENT '价格',
    description TEXT COMMENT '图书简介',
    cover_url VARCHAR(255) COMMENT '封面图片地址',
    category_id BIGINT COMMENT '分类ID',
    tags VARCHAR(255) COMMENT '标签（逗号分隔）',
    shelf_location VARCHAR(50) COMMENT '架位号',
    rfid_tag VARCHAR(100) COMMENT 'RFID标签号（AES加密存储）',
    barcode VARCHAR(50) COMMENT '条形码',
    call_number VARCHAR(50) COMMENT '索书号',
    entry_date DATE COMMENT '入库日期',
    source VARCHAR(50) COMMENT '来源：purchase/donation/exchange',
    donor VARCHAR(50) COMMENT '捐赠者',
    collection_status VARCHAR(20) DEFAULT 'on_shelf' COMMENT '馆藏状态：on_shelf/borrowed/reserved/maintenance/lost/damaged',
    is_borrowable TINYINT DEFAULT 1 COMMENT '是否可借：0否 1是',
    current_borrower_id BIGINT COMMENT '当前借阅者ID',
    due_date DATE COMMENT '应还日期',
    total_borrow_count INT DEFAULT 0 COMMENT '累计借阅次数',
    last_borrow_time DATETIME COMMENT '最近借出时间',
    last_return_time DATETIME COMMENT '最近归还时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建人ID',
    updater_id BIGINT COMMENT '更新人ID',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_book_name (book_name),
    INDEX idx_isbn (isbn),
    INDEX idx_rfid_tag (rfid_tag),
    INDEX idx_category_id (category_id),
    INDEX idx_collection_status (collection_status),
    INDEX idx_current_borrower (current_borrower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- ============================================
-- 7. 借阅记录表
-- ============================================
CREATE TABLE borrow_records (
    borrow_record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借阅记录ID',
    borrow_no VARCHAR(50) NOT NULL UNIQUE COMMENT '借阅编号',
    user_id BIGINT NOT NULL COMMENT '借阅用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    borrow_time DATETIME NOT NULL COMMENT '借阅时间',
    due_date DATE NOT NULL COMMENT '应还日期',
    return_time DATETIME COMMENT '实际归还时间',
    borrow_type VARCHAR(20) DEFAULT 'normal' COMMENT '借阅类型：normal/renew/inter_library',
    renew_count INT DEFAULT 0 COMMENT '续借次数',
    borrow_status VARCHAR(20) DEFAULT 'borrowed' COMMENT '借阅状态：borrowed/returned/overdue/lost',
    is_overdue TINYINT DEFAULT 0 COMMENT '是否逾期：0否 1是',
    operator_id BIGINT COMMENT '操作人ID',
    operation_time DATETIME COMMENT '操作时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) COMMENT '备注',
    rfid_device_id VARCHAR(50) COMMENT 'RFID设备ID',
    rfid_device_ip VARCHAR(50) COMMENT 'RFID设备IP',
    rfid_read_status VARCHAR(20) COMMENT 'RFID读取状态：success/fail/timeout',
    rfid_read_time DATETIME COMMENT 'RFID读取时间',
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_borrow_no (borrow_no),
    INDEX idx_borrow_status (borrow_status),
    INDEX idx_borrow_time (borrow_time),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- ============================================
-- 8. RFID绑定历史表
-- ============================================
CREATE TABLE rfid_bind_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    rfid_tag VARCHAR(100) NOT NULL COMMENT 'RFID标签号',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    unbind_time DATETIME COMMENT '解绑时间',
    operator_id BIGINT COMMENT '操作人ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：bind/unbind/replace',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_book_id (book_id),
    INDEX idx_rfid_tag (rfid_tag),
    INDEX idx_bind_time (bind_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RFID绑定历史表';

-- ============================================
-- 9. 审计日志表
-- ============================================
CREATE TABLE audit_logs (
    audit_log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审计日志ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(200) COMMENT '操作描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    result_code INT COMMENT '结果状态码',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    log_level VARCHAR(20) DEFAULT 'INFO' COMMENT '日志级别：INFO/ERROR/SECURITY',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(200) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数（AES加密存储）',
    response_data TEXT COMMENT '响应数据',
    execution_time BIGINT COMMENT '执行耗时（毫秒）',
    error_message TEXT COMMENT '错误信息',
    module_name VARCHAR(50) COMMENT '模块名称',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_level_time (log_level, create_time),
    INDEX idx_ip_time (ip_address, create_time),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ============================================
-- 10. 异常记录表
-- ============================================
CREATE TABLE exceptions (
    exception_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '异常ID',
    exception_type VARCHAR(50) NOT NULL COMMENT '异常类型',
    exception_level VARCHAR(20) DEFAULT 'error' COMMENT '异常级别：info/warn/error',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    exception_source VARCHAR(100) COMMENT '异常来源',
    exception_message VARCHAR(500) COMMENT '异常信息',
    exception_details TEXT COMMENT '异常详情',
    user_id BIGINT COMMENT '关联用户ID',
    username VARCHAR(50) COMMENT '关联用户名',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    request_url VARCHAR(200) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_summary VARCHAR(500) COMMENT '请求摘要',
    stack_trace_summary TEXT COMMENT '堆栈摘要',
    resolved_status TINYINT DEFAULT 0 COMMENT '处理状态：0未处理 1已处理 2忽略',
    resolver_id BIGINT COMMENT '处理人ID',
    resolve_time DATETIME COMMENT '处理时间',
    resolve_note VARCHAR(500) COMMENT '处理说明',
    INDEX idx_exception_type (exception_type),
    INDEX idx_exception_level (exception_level),
    INDEX idx_create_time (create_time),
    INDEX idx_resolved_status (resolved_status),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常记录表';

-- ============================================
-- 11. 图书分类表
-- ============================================
CREATE TABLE book_categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
    parent_category_id BIGINT DEFAULT 0 COMMENT '父分类ID，0为顶级分类',
    category_level INT DEFAULT 1 COMMENT '分类层级',
    sort_order INT DEFAULT 0 COMMENT '排序值，越小越靠前',
    description VARCHAR(200) COMMENT '分类描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建人ID',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_parent_id (parent_category_id),
    INDEX idx_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- ============================================
-- 12. 图书预约表
-- ============================================
CREATE TABLE book_reservations (
    reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '预约用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    reservation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
    expire_time DATETIME COMMENT '预约过期时间',
    pickup_deadline DATETIME COMMENT '取书截止时间',
    pickup_time DATETIME COMMENT '实际取书时间',
    cancel_time DATETIME COMMENT '取消时间',
    reservation_status VARCHAR(20) DEFAULT 'pending' COMMENT '预约状态：pending/available/picked_up/cancelled/expired',
    notification_sent TINYINT DEFAULT 0 COMMENT '是否已通知取书：0否 1是',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (reservation_status),
    INDEX idx_reservation_time (reservation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书预约表';

-- ============================================
-- 13. 罚款记录表
-- ============================================
CREATE TABLE fine_records (
    fine_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '罚款ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    borrow_record_id BIGINT COMMENT '关联借阅记录ID',
    fine_type VARCHAR(20) NOT NULL COMMENT '罚款类型：overdue/damage/lost',
    fine_amount DECIMAL(10,2) NOT NULL COMMENT '罚款金额',
    paid_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '已付金额',
    fine_status VARCHAR(20) DEFAULT 'unpaid' COMMENT '罚款状态：unpaid/paid/waived',
    fine_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '罚款时间',
    pay_time DATETIME COMMENT '支付时间',
    overdue_days INT DEFAULT 0 COMMENT '逾期天数',
    operator_id BIGINT COMMENT '操作人ID',
    waive_reason VARCHAR(200) COMMENT '减免原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_user_id (user_id),
    INDEX idx_borrow_record_id (borrow_record_id),
    INDEX idx_fine_status (fine_status),
    INDEX idx_fine_time (fine_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款记录表';

-- ============================================
-- 14. 图书状态变更历史表
-- ============================================
CREATE TABLE book_status_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    old_status VARCHAR(20) COMMENT '变更前状态',
    new_status VARCHAR(20) NOT NULL COMMENT '变更后状态',
    change_reason VARCHAR(200) COMMENT '变更原因',
    operator_id BIGINT COMMENT '操作人ID',
    rfid_device_id VARCHAR(50) COMMENT 'RFID设备ID',
    rfid_device_ip VARCHAR(50) COMMENT 'RFID设备IP',
    change_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_book_id (book_id),
    INDEX idx_change_time (change_time),
    INDEX idx_new_status (new_status),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书状态变更历史表';

-- ============================================
-- 初始化数据：角色
-- ============================================
INSERT INTO roles (role_name, role_code, description, role_level, is_system_role, sort_order) VALUES
('系统管理员', 'system_admin', '系统最高权限管理员', 1, 1, 1),
('超级管理员', 'super_admin', '图书馆超级管理员', 2, 1, 2),
('普通馆员', 'librarian', '图书馆普通工作人员', 3, 1, 3),
('读者', 'reader', '普通读者用户', 4, 1, 4),
('访客', 'visitor', '未注册访客', 5, 1, 5);

-- ============================================
-- 初始化数据：默认管理员账号
-- 密码: admin123 (BCrypt加密)
-- ============================================
INSERT INTO users (username, password, real_name, account_status, max_borrow_count) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, 0);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1);

-- ============================================
-- 初始化数据：权限（十大模块管理权限）
-- ============================================

-- 顶级菜单权限
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

-- 读者中心子权限（假设顶级菜单从ID=1开始，读者中心 ID=3）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, sort_order) VALUES
('个人资料', 'reader:profile', 'menu', 3, '/reader/profile', 1),
('我的借阅', 'reader:borrows', 'menu', 3, '/reader/borrows', 2),
('我的预约', 'reader:reservations', 'menu', 3, '/reader/reservations', 3),
('我的罚款', 'reader:fines', 'menu', 3, '/reader/fines', 4);

-- 图书管理子权限（图书管理 ID=4）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('图书查看', 'book:view', 'button', 4, '/librarian/books', 'GET', 1),
('图书新增', 'book:create', 'button', 4, '/librarian/books', 'POST', 2),
('图书编辑', 'book:update', 'button', 4, '/librarian/books', 'PUT', 3),
('图书删除', 'book:delete', 'button', 4, '/librarian/books', 'DELETE', 4);

-- 分类管理子权限（分类管理 ID=5）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('分类查看', 'category:view', 'button', 5, '/librarian/categories', 'GET', 1),
('分类新增', 'category:create', 'button', 5, '/librarian/categories', 'POST', 2),
('分类编辑', 'category:update', 'button', 5, '/librarian/categories', 'PUT', 3),
('分类删除', 'category:delete', 'button', 5, '/librarian/categories', 'DELETE', 4);

-- 借阅管理子权限（借阅管理 ID=6）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('借阅查看', 'borrow:view', 'button', 6, '/librarian/borrows', 'GET', 1),
('借阅操作', 'borrow:operate', 'button', 6, '/librarian/borrows', 'POST', 2);

-- 预约管理子权限（预约管理 ID=7）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('预约查看', 'reservation:view', 'button', 7, '/librarian/reservations', 'GET', 1),
('预约处理', 'reservation:operate', 'button', 7, '/librarian/reservations', 'PUT', 2);

-- 罚款管理子权限（罚款管理 ID=8）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('罚款查看', 'fine:view', 'button', 8, '/librarian/fines', 'GET', 1),
('罚款操作', 'fine:operate', 'button', 8, '/librarian/fines', 'POST', 2);

-- 用户管理子权限（用户管理 ID=9）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('用户查看', 'user:view', 'button', 9, '/admin/users', 'GET', 1),
('用户新增', 'user:create', 'button', 9, '/admin/users', 'POST', 2),
('用户编辑', 'user:update', 'button', 9, '/admin/users', 'PUT', 3),
('用户删除', 'user:delete', 'button', 9, '/admin/users', 'DELETE', 4);

-- 角色管理子权限（角色管理 ID=10）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('角色查看', 'role:view', 'button', 10, '/admin/roles', 'GET', 1),
('角色新增', 'role:create', 'button', 10, '/admin/roles', 'POST', 2),
('角色编辑', 'role:update', 'button', 10, '/admin/roles', 'PUT', 3),
('角色删除', 'role:delete', 'button', 10, '/admin/roles', 'DELETE', 4);

-- 权限管理子权限（权限管理 ID=11）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('权限查看', 'permission:view', 'button', 11, '/admin/permissions', 'GET', 1),
('权限新增', 'permission:create', 'button', 11, '/admin/permissions', 'POST', 2),
('权限编辑', 'permission:update', 'button', 11, '/admin/permissions', 'PUT', 3),
('权限删除', 'permission:delete', 'button', 11, '/admin/permissions', 'DELETE', 4);

-- 审计日志子权限（审计日志 ID=12）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('日志查看', 'audit:view', 'button', 12, '/admin/audit-logs', 'GET', 1),
('日志清理', 'audit:clean', 'button', 12, '/admin/audit-logs', 'DELETE', 2);

-- 异常管理子权限（异常管理 ID=13）
INSERT INTO permissions (permission_name, permission_code, permission_type, parent_permission_id, url_path, http_method, sort_order) VALUES
('异常查看', 'exception:view', 'button', 13, '/admin/exceptions', 'GET', 1),
('异常处理', 'exception:resolve', 'button', 13, '/admin/exceptions', 'POST', 2);

-- ============================================
-- 初始化数据：图书分类（中图法基本大类）
-- ============================================
INSERT INTO book_categories (category_name, category_code, category_level, sort_order) VALUES
('马克思主义、列宁主义、毛泽东思想、邓小平理论', 'A', 1, 1),
('哲学、宗教', 'B', 1, 2),
('社会科学总论', 'C', 1, 3),
('政治、法律', 'D', 1, 4),
('军事', 'E', 1, 5),
('经济', 'F', 1, 6),
('文化、科学、教育、体育', 'G', 1, 7),
('语言、文字', 'H', 1, 8),
('文学', 'I', 1, 9),
('艺术', 'J', 1, 10),
('历史、地理', 'K', 1, 11),
('自然科学总论', 'N', 1, 12),
('数理科学和化学', 'O', 1, 13),
('天文学、地球科学', 'P', 1, 14),
('生物科学', 'Q', 1, 15),
('医药、卫生', 'R', 1, 16),
('农业科学', 'S', 1, 17),
('工业技术', 'T', 1, 18),
('交通运输', 'U', 1, 19),
('航空、航天', 'V', 1, 20),
('环境科学、安全科学', 'X', 1, 21),
('综合性图书', 'Z', 1, 22);
