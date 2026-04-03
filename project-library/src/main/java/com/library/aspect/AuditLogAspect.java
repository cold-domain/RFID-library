package com.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.utils.ContextHolder;
import com.library.common.utils.RequestTraceUtils;
import com.library.entity.AuditLog;
import com.library.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("execution(public * com.library.controller..*(..))")
    public Object recordAuditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        String operationType = RequestTraceUtils.resolveOperationType(request.getMethod(), request.getRequestURI());
        AuditLog log = new AuditLog();
        log.setOperationType(operationType);
        log.setOperationDesc(RequestTraceUtils.resolveOperationDesc(request.getMethod(), request.getRequestURI()));
        log.setModuleName(RequestTraceUtils.resolveModuleName(request.getRequestURI()));
        log.setRequestMethod(request.getMethod());
        log.setRequestUrl(request.getRequestURI());
        log.setIpAddress(RequestTraceUtils.getClientIp(request));
        log.setUserAgent(RequestTraceUtils.truncate(request.getHeader("User-Agent"), 500));
        log.setRequestParams(RequestTraceUtils.summarizeArguments(joinPoint.getArgs(), objectMapper));

        if (ContextHolder.getCurrentUserId() != null) {
            log.setUserId(ContextHolder.getCurrentUserId());
        }
        String username = ContextHolder.getCurrentUsername();
        if (username == null) {
            username = RequestTraceUtils.extractUsernameFromArgs(joinPoint.getArgs(), objectMapper);
        }
        log.setUsername(username);

        try {
            Object result = joinPoint.proceed();
            log.setResultCode(RequestTraceUtils.resolveResultCode(result));
            log.setLogLevel(RequestTraceUtils.resolveLogLevel(operationType, log.getResultCode(), null));
            log.setResponseData(RequestTraceUtils.summarizeResult(result, objectMapper));
            return result;
        } catch (Throwable throwable) {
            log.setResultCode(RequestTraceUtils.resolveErrorCode(throwable));
            log.setLogLevel(RequestTraceUtils.resolveLogLevel(operationType, log.getResultCode(), throwable));
            log.setErrorMessage(RequestTraceUtils.truncate(throwable.getMessage(), 1000));
            throw throwable;
        } finally {
            log.setExecutionTime(System.currentTimeMillis() - start);
            try {
                auditLogService.recordLog(log);
            } catch (Exception ignored) {
                // 审计日志记录失败不能影响主业务
            }
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }
}
