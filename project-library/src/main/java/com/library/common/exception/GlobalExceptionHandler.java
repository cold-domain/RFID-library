package com.library.common.exception;

import com.library.common.utils.ContextHolder;
import com.library.common.utils.RequestTraceUtils;
import com.library.common.vo.Result;
import com.library.entity.ExceptionRecord;
import com.library.service.ExceptionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "\u53c2\u6570\u6821\u9a8c\u5931\u8d25";
        recordException(request, e, message);
        return Result.error(400, message);
    }

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e, HttpServletRequest request) {
        recordException(request, e, e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        recordException(request, e, e.getMessage());
        return Result.error(500, "\u7cfb\u7edf\u5185\u90e8\u9519\u8bef: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        recordException(request, e, e.getMessage());
        return Result.error(500, "\u7cfb\u7edf\u5f02\u5e38: " + e.getMessage());
    }

    private void recordException(HttpServletRequest request, Throwable throwable, String message) {
        try {
            ExceptionRecord record = new ExceptionRecord();
            record.setExceptionType(RequestTraceUtils.resolveExceptionType(request, throwable));
            record.setExceptionLevel(RequestTraceUtils.resolveExceptionLevel(throwable));
            record.setExceptionSource(RequestTraceUtils.resolveModuleName(request != null ? request.getRequestURI() : null));
            record.setExceptionMessage(RequestTraceUtils.truncate(message, 500));
            record.setExceptionDetails(RequestTraceUtils.summarizeThrowable(throwable));
            record.setStackTraceSummary(RequestTraceUtils.summarizeThrowable(throwable));
            record.setUserId(ContextHolder.getCurrentUserId());
            record.setUsername(ContextHolder.getCurrentUsername());
            if (request != null) {
                record.setIpAddress(RequestTraceUtils.getClientIp(request));
                record.setUserAgent(RequestTraceUtils.truncate(request.getHeader("User-Agent"), 500));
                record.setRequestUrl(request.getRequestURI());
                record.setRequestMethod(request.getMethod());
                record.setRequestSummary(RequestTraceUtils.buildRequestSummary(request));
            }
            exceptionRecordService.recordException(record);
        } catch (Exception ignored) {
            // exception logging must never break the response flow
        }
    }
}
