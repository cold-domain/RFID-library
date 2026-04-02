package com.library.common.constant;

/**
 * 系统常量类
 */
public class Constants {

    // 系统配置常量
    public static final String SYSTEM_NAME = "图书馆RFID数据安全管理系统";
    public static final String VERSION = "1.0.0";

    // JWT相关常量
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_SECRET = "library_rfid_security_secret_key_2024";
    public static final long JWT_EXPIRATION = 86400000L; // 24小时

    // 加密相关常量
    public static final String AES_KEY = "library_rfid_encryption_key_2024";
    public static final String AES_IV = "library_rfid_encryption_iv_2024";

    // 数据库相关常量
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/library_rfid?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "123456";

    // Redis相关常量
    public static final String REDIS_HOST = "localhost";
    public static final int REDIS_PORT = 6379;
    public static final int REDIS_DATABASE = 0;

    // 用户角色常量
    public static final String ROLE_SYSTEM_ADMIN = "system_admin";
    public static final String ROLE_SUPER_ADMIN = "super_admin";
    public static final String ROLE_LIBRARIAN = "librarian";
    public static final String ROLE_READER = "reader";
    public static final String ROLE_VISITOR = "visitor";

    // 图书状态常量
    public static final String BOOK_STATUS_ON_SHELF = "on_shelf";      // 在架
    public static final String BOOK_STATUS_BORROWED = "borrowed";     // 已借出
    public static final String BOOK_STATUS_EXCEPTION = "exception";   // 异常状态

    // 操作类型常量
    public static final String OPERATION_TYPE_LOGIN = "login";
    public static final String OPERATION_TYPE_LOGOUT = "logout";
    public static final String OPERATION_TYPE_BORROW = "borrow";
    public static final String OPERATION_TYPE_RETURN = "return";
    public static final String OPERATION_TYPE_RENEW = "renew";
    public static final String OPERATION_TYPE_RESERVE = "reserve";
    public static final String OPERATION_TYPE_ADD_BOOK = "add_book";
    public static final String OPERATION_TYPE_UPDATE_BOOK = "update_book";
    public static final String OPERATION_TYPE_DELETE_BOOK = "delete_book";
    public static final String OPERATION_TYPE_ADD_USER = "add_user";
    public static final String OPERATION_TYPE_UPDATE_USER = "update_user";
    public static final String OPERATION_TYPE_DELETE_USER = "delete_user";
    public static final String OPERATION_TYPE_UPDATE_PERMISSION = "update_permission";

    // 文件上传相关常量
    public static final String UPLOAD_PATH = "./uploads";
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // 通用状态码
    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;

    // 登录失败锁定配置
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOGIN_LOCK_TIME = 300; // 5分钟

    // 接口限流配置
    public static final int RATE_LIMIT_REQUESTS = 100;
    public static final int RATE_LIMIT_DURATION = 60; // 每分钟

    // 日志保留天数
    public static final int LOG_RETENTION_DAYS = 30;

    private Constants() {
        // 私有构造函数，防止实例化
    }
}