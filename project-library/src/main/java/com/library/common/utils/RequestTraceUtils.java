package com.library.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.constant.Constants;
import com.library.common.exception.BusinessException;
import com.library.common.vo.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RequestTraceUtils {

    private static final int TEXT_LIMIT = 1000;
    private static final int STACK_LIMIT = 3000;
    private static final Set<String> SENSITIVE_KEYS = new LinkedHashSet<>();

    static {
        SENSITIVE_KEYS.add("password");
        SENSITIVE_KEYS.add("oldPassword");
        SENSITIVE_KEYS.add("newPassword");
        SENSITIVE_KEYS.add("confirmPassword");
        SENSITIVE_KEYS.add("token");
        SENSITIVE_KEYS.add("authorization");
        SENSITIVE_KEYS.add("rfidTag");
    }

    private RequestTraceUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    public static String resolveModuleName(String uri) {
        if (!StringUtils.hasText(uri)) {
            return "系统";
        }
        if (uri.startsWith("/auth")) return "认证中心";
        if (uri.startsWith("/dashboard")) return "控制中心";
        if (uri.startsWith("/books/public")) return "图书搜索";
        if (uri.startsWith("/reader/profile")) return "个人信息";
        if (uri.startsWith("/reader/borrows")) return "我的借阅";
        if (uri.startsWith("/reader/reservations")) return "我的预约";
        if (uri.startsWith("/reader/fines")) return "我的罚款";
        if (uri.startsWith("/reader")) return "读者中心";
        if (uri.startsWith("/librarian/books")) return "图书管理";
        if (uri.startsWith("/librarian/categories")) return "分类管理";
        if (uri.startsWith("/librarian/borrows")) return "借阅管理";
        if (uri.startsWith("/librarian/reservations")) return "预约管理";
        if (uri.startsWith("/librarian/fines")) return "罚款管理";
        if (uri.startsWith("/admin/users")) return "用户管理";
        if (uri.startsWith("/admin/roles")) return "角色管理";
        if (uri.startsWith("/admin/permissions")) return "权限管理";
        if (uri.startsWith("/admin/audit-logs")) return "审计日志";
        if (uri.startsWith("/admin/exceptions")) return "异常管理";
        return "系统";
    }

    public static String resolveOperationType(String method, String uri) {
        if (!StringUtils.hasText(uri)) {
            return "unknown";
        }
        if ("POST".equalsIgnoreCase(method) && "/auth/login".equals(uri)) return Constants.OPERATION_TYPE_LOGIN;
        if ("POST".equalsIgnoreCase(method) && uri.endsWith("/borrow")) return Constants.OPERATION_TYPE_BORROW;
        if ("POST".equalsIgnoreCase(method) && uri.contains("/return")) return Constants.OPERATION_TYPE_RETURN;
        if ("POST".equalsIgnoreCase(method) && uri.contains("/renew")) return Constants.OPERATION_TYPE_RENEW;
        if ("POST".equalsIgnoreCase(method) && (uri.contains("/reservations") || uri.contains("/pickup"))) return Constants.OPERATION_TYPE_RESERVE;
        if ("POST".equalsIgnoreCase(method) && "/librarian/books".equals(uri)) return Constants.OPERATION_TYPE_ADD_BOOK;
        if ("PUT".equalsIgnoreCase(method) && uri.startsWith("/librarian/books/")) return Constants.OPERATION_TYPE_UPDATE_BOOK;
        if ("DELETE".equalsIgnoreCase(method) && uri.startsWith("/librarian/books/")) return Constants.OPERATION_TYPE_DELETE_BOOK;
        if ("POST".equalsIgnoreCase(method) && "/admin/users".equals(uri)) return Constants.OPERATION_TYPE_ADD_USER;
        if ("PUT".equalsIgnoreCase(method) && uri.startsWith("/admin/users/")) return Constants.OPERATION_TYPE_UPDATE_USER;
        if ("DELETE".equalsIgnoreCase(method) && uri.startsWith("/admin/users/")) return Constants.OPERATION_TYPE_DELETE_USER;
        if ((uri.startsWith("/admin/permissions") || uri.contains("/permissions")) && !"GET".equalsIgnoreCase(method)) {
            return Constants.OPERATION_TYPE_UPDATE_PERMISSION;
        }
        return method.toLowerCase() + ":" + uri;
    }

    public static String resolveOperationDesc(String method, String uri) {
        return method + " " + uri;
    }

    public static String summarizeArguments(Object[] args, ObjectMapper objectMapper) {
        if (args == null || args.length == 0) {
            return "";
        }
        List<Object> values = new ArrayList<>();
        for (Object arg : args) {
            if (shouldSkipArgument(arg)) {
                continue;
            }
            values.add(sanitizeObject(arg, objectMapper));
        }
        return truncate(toJson(values, objectMapper), TEXT_LIMIT);
    }

    public static String summarizeResult(Object result, ObjectMapper objectMapper) {
        if (result == null) {
            return "";
        }
        if (result instanceof Result) {
            Result<?> wrapped = (Result<?>) result;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("code", wrapped.getCode());
            data.put("message", wrapped.getMessage());
            return truncate(toJson(data, objectMapper), TEXT_LIMIT);
        }
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", responseEntity.getStatusCodeValue());
            data.put("body", sanitizeObject(responseEntity.getBody(), objectMapper));
            return truncate(toJson(data, objectMapper), TEXT_LIMIT);
        }
        return truncate(toJson(sanitizeObject(result, objectMapper), objectMapper), TEXT_LIMIT);
    }

    public static Integer resolveResultCode(Object result) {
        if (result instanceof Result) {
            return ((Result<?>) result).getCode();
        }
        if (result instanceof ResponseEntity) {
            return ((ResponseEntity<?>) result).getStatusCodeValue();
        }
        return Constants.SUCCESS;
    }

    public static Integer resolveErrorCode(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            return ((BusinessException) throwable).getCode();
        }
        if (throwable instanceof AccessDeniedException) {
            return Constants.FORBIDDEN;
        }
        if (throwable instanceof AuthenticationException) {
            return Constants.UNAUTHORIZED;
        }
        return Constants.ERROR;
    }

    public static String resolveLogLevel(String operationType, Integer resultCode, Throwable throwable) {
        if (throwable instanceof AccessDeniedException || throwable instanceof AuthenticationException) {
            return "SECURITY";
        }
        if (throwable != null || (resultCode != null && resultCode >= 500)) {
            return "ERROR";
        }
        if (Constants.OPERATION_TYPE_LOGIN.equals(operationType) || Constants.OPERATION_TYPE_LOGOUT.equals(operationType)) {
            return "SECURITY";
        }
        if (resultCode != null && (resultCode == Constants.UNAUTHORIZED || resultCode == Constants.FORBIDDEN)) {
            return "SECURITY";
        }
        return "INFO";
    }

    public static String resolveExceptionType(HttpServletRequest request, Throwable throwable) {
        String uri = request != null ? request.getRequestURI() : "";
        if (throwable instanceof AccessDeniedException || throwable instanceof AuthenticationException || uri.startsWith("/auth")) {
            return "security";
        }
        if (uri.contains("rfid")) {
            return "rfid";
        }
        if (throwable instanceof BusinessException || throwable instanceof IllegalArgumentException) {
            return "business";
        }
        return "system";
    }

    public static String resolveExceptionLevel(Throwable throwable) {
        if (throwable instanceof AccessDeniedException || throwable instanceof AuthenticationException) {
            return "high";
        }
        if (throwable instanceof BusinessException || throwable instanceof IllegalArgumentException) {
            return "medium";
        }
        return "critical";
    }

    public static String summarizeThrowable(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return truncate(writer.toString(), STACK_LIMIT);
    }

    public static String buildRequestSummary(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String query = request.getQueryString();
        if (StringUtils.hasText(query)) {
            return truncate(request.getMethod() + " " + request.getRequestURI() + "?" + query, TEXT_LIMIT);
        }
        return truncate(request.getMethod() + " " + request.getRequestURI(), TEXT_LIMIT);
    }

    public static String extractUsernameFromArgs(Object[] args, ObjectMapper objectMapper) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            Object sanitized = sanitizeObject(arg, objectMapper);
            String username = findFieldValue(sanitized, "username");
            if (StringUtils.hasText(username)) {
                return username;
            }
        }
        return null;
    }

    public static String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    public static String nowText() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static boolean shouldSkipArgument(Object arg) {
        if (arg == null) {
            return true;
        }
        return arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof BindingResult
                || arg instanceof MultipartFile;
    }

    private static Object sanitizeObject(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map) {
            Map<?, ?> source = (Map<?, ?>) value;
            Map<String, Object> target = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                String key = String.valueOf(entry.getKey());
                target.put(key, isSensitive(key) ? "***" : sanitizeObject(entry.getValue(), objectMapper));
            }
            return target;
        }
        if (value instanceof Collection) {
            List<Object> target = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                target.add(sanitizeObject(item, objectMapper));
            }
            return target;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> target = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                target.add(sanitizeObject(Array.get(value, i), objectMapper));
            }
            return target;
        }
        if (!ClassUtils.isPrimitiveOrWrapper(value.getClass()) && !value.getClass().getName().startsWith("java.")) {
            Object converted = objectMapper.convertValue(value, Object.class);
            return sanitizeObject(converted, objectMapper);
        }
        return value.toString();
    }

    private static String toJson(Object value, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return truncate(String.valueOf(value), TEXT_LIMIT);
        }
    }

    private static boolean isSensitive(String key) {
        for (String sensitiveKey : SENSITIVE_KEYS) {
            if (sensitiveKey.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private static String findFieldValue(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            Object nested = ((Map<?, ?>) value).get(fieldName);
            if (nested instanceof String && StringUtils.hasText((String) nested)) {
                return (String) nested;
            }
            for (Object item : ((Map<?, ?>) value).values()) {
                String found = findFieldValue(item, fieldName);
                if (StringUtils.hasText(found)) {
                    return found;
                }
            }
        } else if (value instanceof Collection) {
            for (Object item : (Collection<?>) value) {
                String found = findFieldValue(item, fieldName);
                if (StringUtils.hasText(found)) {
                    return found;
                }
            }
        }
        return null;
    }
}
