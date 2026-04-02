package com.library.common.vo;

import com.library.common.constant.Constants;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    private Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(Constants.SUCCESS, "操作成功", null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Constants.SUCCESS, "操作成功", data);
    }

    /**
     * 成功响应，自定义消息
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(Constants.SUCCESS, message, null);
    }

    /**
     * 成功响应，自定义消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(Constants.SUCCESS, message, data);
    }

    /**
     * 错误响应
     */
    public static <T> Result<T> error() {
        return new Result<>(Constants.ERROR, "操作失败", null);
    }

    /**
     * 错误响应，自定义错误码和消息
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 错误响应，自定义消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(Constants.ERROR, message, null);
    }

    /**
     * 错误响应，带数据
     */
    public static <T> Result<T> error(T data) {
        return new Result<>(Constants.ERROR, "操作失败", data);
    }
}