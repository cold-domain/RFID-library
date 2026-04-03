package com.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.constant.Constants;
import com.library.common.exception.BusinessException;
import com.library.common.exception.GlobalExceptionHandler;
import com.library.common.utils.*;
import com.library.common.vo.Result;
import com.library.entity.AuditLog;
import com.library.entity.ExceptionRecord;
import com.library.entity.User;
import com.library.security.handler.CustomAccessDeniedHandler;
import com.library.security.handler.CustomAuthenticationEntryPoint;
import com.library.security.handler.CustomAuthenticationSuccessHandler;
import com.library.service.AuditLogService;
import com.library.service.ExceptionRecordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.library.TestSupport.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultTest {

    @Test
    void successFactoriesPopulateFields() {
        Result<String> empty = Result.success();
        Result<Integer> withData = Result.success(123);
        Result<String> withMessage = Result.success("done");
        Result<String> withBoth = Result.success("done", "payload");

        assertThat(empty.getCode()).isEqualTo(Constants.SUCCESS);
        assertThat(withData.getData()).isEqualTo(123);
        assertThat(withMessage.getMessage()).isEqualTo("done");
        assertThat(withBoth.getMessage()).isEqualTo("done");
        assertThat(withBoth.getData()).isEqualTo("payload");
    }

    @Test
    void errorFactoriesPopulateFields() {
        Result<String> empty = Result.error();
        Result<String> withCode = Result.error(400, "bad");
        Result<String> withMessage = Result.error("bad");
        Result<Integer> withData = Result.error(123);

        assertThat(empty.getCode()).isEqualTo(Constants.ERROR);
        assertThat(withCode.getCode()).isEqualTo(400);
        assertThat(withCode.getMessage()).isEqualTo("bad");
        assertThat(withMessage.getMessage()).isEqualTo("bad");
        assertThat(withData.getData()).isEqualTo(123);
    }
}

class ContextHolderTest {

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void storesAndClearsCurrentUser() {
        User currentUser = user(1L, "alice");

        ContextHolder.setCurrentUser(currentUser);

        assertThat(ContextHolder.getCurrentUser()).isSameAs(currentUser);
        assertThat(ContextHolder.getCurrentUserId()).isEqualTo(1L);
        assertThat(ContextHolder.getCurrentUsername()).isEqualTo("alice");

        ContextHolder.clear();

        assertThat(ContextHolder.getCurrentUser()).isNull();
        assertThat(ContextHolder.getCurrentUserId()).isNull();
        assertThat(ContextHolder.getCurrentUsername()).isNull();
    }
}

class BCryptPasswordEncoderUtilsTest {

    @Test
    void encodesAndMatchesPassword() {
        String encoded = BCryptPasswordEncoderUtils.encode("secret");

        assertThat(BCryptPasswordEncoderUtils.matches("secret", encoded)).isTrue();
        assertThat(BCryptPasswordEncoderUtils.matches("other", encoded)).isFalse();
    }

    @Test
    void existingHashDoesNotNeedUpgrade() {
        String encoded = BCryptPasswordEncoderUtils.encode("secret");

        assertThat(BCryptPasswordEncoderUtils.needsUpgrade(encoded)).isFalse();
    }
}

class EncryptionUtilsTest {

    private final EncryptionUtils encryptionUtils = new EncryptionUtils();

    @Test
    void aesEncryptAndDecryptRoundTrip() {
        String encrypted = encryptionUtils.aesEncrypt("hello");

        assertThat(encrypted).isNotBlank();
        assertThat(encryptionUtils.aesDecrypt(encrypted)).isEqualTo("hello");
    }

    @Test
    void passwordHelpersUseBcrypt() {
        String encoded = encryptionUtils.passwordEncrypt("secret");

        assertThat(encryptionUtils.passwordVerify("secret", encoded)).isTrue();
        assertThat(encryptionUtils.passwordVerify("other", encoded)).isFalse();
    }
}

class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils();

    @Test
    void generatesAndParsesToken() {
        String token = jwtUtils.generateToken("alice", "reader");

        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("alice");
        assertThat(jwtUtils.getRoleFromToken(token)).isEqualTo("reader");
        assertThat(jwtUtils.isTokenExpired(token)).isFalse();
    }

    @Test
    void validateReturnsFalseForInvalidToken() {
        assertThat(jwtUtils.validateToken("invalid-token")).isFalse();
    }
}

class RequestTraceUtilsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void resolvesClientIpAndModuleNameAndOperationType() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");

        assertThat(RequestTraceUtils.getClientIp(request)).isEqualTo("1.2.3.4");
        assertThat(RequestTraceUtils.resolveModuleName("/librarian/books/1")).isNotBlank();
        assertThat(RequestTraceUtils.resolveModuleName(null)).isNotBlank();
        assertThat(RequestTraceUtils.resolveOperationType("POST", "/auth/login")).isEqualTo(Constants.OPERATION_TYPE_LOGIN);
        assertThat(RequestTraceUtils.resolveOperationType("PUT", "/admin/users/1")).isEqualTo(Constants.OPERATION_TYPE_UPDATE_USER);
        assertThat(RequestTraceUtils.resolveOperationType("GET", "/x")).isEqualTo("get:/x");
        assertThat(RequestTraceUtils.resolveOperationDesc("GET", "/x")).isEqualTo("GET /x");
    }

    @Test
    void summarizeArgumentsMasksSensitiveValuesAndSkipsServletArtifacts() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        Object[] args = {
                Map.of("username", "alice", "password", "secret", "token", "abc"),
                request,
                bindingResult
        };

        String summary = RequestTraceUtils.summarizeArguments(args, objectMapper);

        assertThat(summary).contains("alice");
        assertThat(summary).contains("***");
        assertThat(summary).doesNotContain("secret");
        assertThat(summary).doesNotContain("abc");
    }

    @Test
    void summarizeResultAndResolveResultCodeHandleWrapperTypes() {
        Result<String> result = Result.success("ok", "payload");
        ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.status(201).body(Map.of("token", "abc"));

        String wrappedSummary = RequestTraceUtils.summarizeResult(result, objectMapper);
        String responseSummary = RequestTraceUtils.summarizeResult(responseEntity, objectMapper);

        assertThat(wrappedSummary).contains("\"code\":200");
        assertThat(responseSummary).contains("\"status\":201");
        assertThat(responseSummary).contains("***");
        assertThat(RequestTraceUtils.resolveResultCode(result)).isEqualTo(Constants.SUCCESS);
        assertThat(RequestTraceUtils.resolveResultCode(responseEntity)).isEqualTo(201);
        assertThat(RequestTraceUtils.resolveResultCode("plain")).isEqualTo(Constants.SUCCESS);
    }

    @Test
    void resolvesErrorCodeLogLevelExceptionTypeAndLevel() {
        MockHttpServletRequest authRequest = new MockHttpServletRequest("GET", "/auth/me");
        MockHttpServletRequest rfidRequest = new MockHttpServletRequest("GET", "/librarian/books/1/rfid/history");

        assertThat(RequestTraceUtils.resolveErrorCode(new BusinessException(422, "bad"))).isEqualTo(422);
        assertThat(RequestTraceUtils.resolveErrorCode(new AccessDeniedException("denied"))).isEqualTo(Constants.FORBIDDEN);
        assertThat(RequestTraceUtils.resolveErrorCode(new BadCredentialsException("bad"))).isEqualTo(Constants.UNAUTHORIZED);
        assertThat(RequestTraceUtils.resolveErrorCode(new RuntimeException("boom"))).isEqualTo(Constants.ERROR);

        assertThat(RequestTraceUtils.resolveLogLevel("login", 200, null)).isEqualTo("SECURITY");
        assertThat(RequestTraceUtils.resolveLogLevel("x", 500, null)).isEqualTo("ERROR");
        assertThat(RequestTraceUtils.resolveLogLevel("x", 403, null)).isEqualTo("SECURITY");
        assertThat(RequestTraceUtils.resolveLogLevel("x", 200, null)).isEqualTo("INFO");

        assertThat(RequestTraceUtils.resolveExceptionType(authRequest, new RuntimeException("boom"))).isEqualTo("security");
        assertThat(RequestTraceUtils.resolveExceptionType(rfidRequest, new RuntimeException("boom"))).isEqualTo("rfid");
        assertThat(RequestTraceUtils.resolveExceptionType(new MockHttpServletRequest(), new BusinessException("bad"))).isEqualTo("business");
        assertThat(RequestTraceUtils.resolveExceptionType(new MockHttpServletRequest(), new RuntimeException("boom"))).isEqualTo("system");

        assertThat(RequestTraceUtils.resolveExceptionLevel(new AccessDeniedException("denied"))).isEqualTo("high");
        assertThat(RequestTraceUtils.resolveExceptionLevel(new IllegalArgumentException("bad"))).isEqualTo("medium");
        assertThat(RequestTraceUtils.resolveExceptionLevel(new RuntimeException("boom"))).isEqualTo("critical");
    }

    @Test
    void buildsThrowableRequestSummaryUsernameAndTextHelpers() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/books/public");
        request.setQueryString("keyword=java");
        Object[] args = {Map.of("payload", Map.of("username", "alice"))};

        String throwableSummary = RequestTraceUtils.summarizeThrowable(new RuntimeException("boom"));

        assertThat(throwableSummary).contains("RuntimeException");
        assertThat(RequestTraceUtils.buildRequestSummary(request)).isEqualTo("GET /books/public?keyword=java");
        assertThat(RequestTraceUtils.extractUsernameFromArgs(args, objectMapper)).isEqualTo("alice");
        assertThat(RequestTraceUtils.truncate("abcdef", 3)).isEqualTo("abc...");
        assertThat(RequestTraceUtils.nowText()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }
}

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ExceptionRecordService exceptionRecordService;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Captor
    private ArgumentCaptor<ExceptionRecord> exceptionCaptor;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void handleValidationExceptionReturns400AndRecordsException() throws Exception {
        MethodArgumentNotValidException exception = CommonAndSecurityTestSupport.validationException("name", "required");
        MockHttpServletRequest request = CommonAndSecurityTestSupport.request("POST", "/admin/users");
        ContextHolder.setCurrentUser(user(1L, "alice"));

        Result<?> result = handler.handleValidationException(exception, request);

        assertThat(result.getCode()).isEqualTo(400);
        verify(exceptionRecordService).recordException(exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getUsername()).isEqualTo("alice");
        assertThat(exceptionCaptor.getValue().getRequestSummary()).isEqualTo("POST /admin/users");
    }

    @Test
    void handleBusinessExceptionReturnsBusinessCodeAndRecords() {
        MockHttpServletRequest request = CommonAndSecurityTestSupport.request("GET", "/reader/profile");

        Result<?> result = handler.handleBusinessException(new BusinessException(409, "exists"), request);

        assertThat(result.getCode()).isEqualTo(409);
        verify(exceptionRecordService).recordException(any(ExceptionRecord.class));
    }

    @Test
    void handleRuntimeExceptionReturns500AndRecords() {
        MockHttpServletRequest request = CommonAndSecurityTestSupport.request("GET", "/dashboard/stats");

        Result<?> result = handler.handleRuntimeException(new RuntimeException("boom"), request);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
        assertThat(result.getMessage()).contains("boom");
        verify(exceptionRecordService).recordException(any(ExceptionRecord.class));
    }

    @Test
    void handleExceptionReturns500AndSwallowsRecordFailure() {
        doThrow(new RuntimeException("ignore")).when(exceptionRecordService).recordException(any(ExceptionRecord.class));
        MockHttpServletRequest request = CommonAndSecurityTestSupport.request("GET", "/dashboard/stats");

        Result<?> result = handler.handleException(new Exception("boom"), request);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
        assertThat(result.getMessage()).contains("boom");
    }
}

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ExceptionRecordService exceptionRecordService;

    @InjectMocks
    private CustomAccessDeniedHandler handler;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;
    @Captor
    private ArgumentCaptor<ExceptionRecord> exceptionCaptor;

    @Test
    void handleWrites403AndRecordsSecurityEvent() throws Exception {
        MockHttpServletRequest request = CommonAndSecurityTestSupport.request("GET", "/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"code\":403");
        verify(auditLogService).recordLog(auditLogCaptor.capture());
        verify(exceptionRecordService).recordException(exceptionCaptor.capture());
        assertThat(auditLogCaptor.getValue().getOperationType()).isEqualTo("forbidden");
        assertThat(exceptionCaptor.getValue().getExceptionType()).isEqualTo("security");
    }
}

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ExceptionRecordService exceptionRecordService;

    @InjectMocks
    private CustomAuthenticationEntryPoint handler;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    @Test
    void commenceWrites401AndRecordsSecurityEvent() throws Exception {
        MockHttpServletRequest request = CommonAndSecurityTestSupport.request("GET", "/reader/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new BadCredentialsException("bad token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":401");
        verify(auditLogService).recordLog(auditLogCaptor.capture());
        verify(exceptionRecordService).recordException(any(ExceptionRecord.class));
        assertThat(auditLogCaptor.getValue().getOperationType()).isEqualTo("unauthorized");
    }
}

class CustomAuthenticationSuccessHandlerTest {

    private final CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler();

    @Test
    void successHandlerLeavesResponseUntouched() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, null);

        assertThat(response.getContentAsString()).isEmpty();
    }
}

final class CommonAndSecurityTestSupport {

    private CommonAndSecurityTestSupport() {
    }

    static MethodArgumentNotValidException validationException(String field, String message) throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", field, message));
        Method method = CommonAndSecurityTestSupport.class.getDeclaredMethod("sampleMethod", String.class);
        return new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(method, 0), bindingResult);
    }

    static void sampleMethod(String value) {
    }

    static MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.addHeader("User-Agent", "JUnit");
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
