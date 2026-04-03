package com.library.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.constant.Constants;
import com.library.common.utils.RequestTraceUtils;
import com.library.common.vo.Result;
import com.library.entity.AuditLog;
import com.library.entity.ExceptionRecord;
import com.library.service.AuditLogService;
import com.library.service.ExceptionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        recordSecurityEvent(request, Constants.UNAUTHORIZED, authException.getMessage(), authException);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(
            Result.error(401, "\u672a\u6388\u6743: " + authException.getMessage())
        ));
    }

    private void recordSecurityEvent(HttpServletRequest request, int code, String message, Throwable throwable) {
        try {
            AuditLog log = new AuditLog();
            log.setOperationType("unauthorized");
            log.setOperationDesc(RequestTraceUtils.resolveOperationDesc(request.getMethod(), request.getRequestURI()));
            log.setModuleName(RequestTraceUtils.resolveModuleName(request.getRequestURI()));
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setIpAddress(RequestTraceUtils.getClientIp(request));
            log.setUserAgent(RequestTraceUtils.truncate(request.getHeader("User-Agent"), 500));
            log.setLogLevel("SECURITY");
            log.setResultCode(code);
            log.setErrorMessage(RequestTraceUtils.truncate(message, 1000));
            auditLogService.recordLog(log);

            ExceptionRecord record = new ExceptionRecord();
            record.setExceptionType("security");
            record.setExceptionLevel("high");
            record.setExceptionSource(RequestTraceUtils.resolveModuleName(request.getRequestURI()));
            record.setExceptionMessage(RequestTraceUtils.truncate(message, 500));
            record.setExceptionDetails(RequestTraceUtils.summarizeThrowable(throwable));
            record.setStackTraceSummary(RequestTraceUtils.summarizeThrowable(throwable));
            record.setIpAddress(RequestTraceUtils.getClientIp(request));
            record.setUserAgent(RequestTraceUtils.truncate(request.getHeader("User-Agent"), 500));
            record.setRequestUrl(request.getRequestURI());
            record.setRequestMethod(request.getMethod());
            record.setRequestSummary(RequestTraceUtils.buildRequestSummary(request));
            exceptionRecordService.recordException(record);
        } catch (Exception ignored) {
            // ignore
        }
    }
}
