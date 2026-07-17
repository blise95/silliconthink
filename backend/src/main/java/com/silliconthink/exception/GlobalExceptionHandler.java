package com.silliconthink.exception;

import com.silliconthink.common.ErrorCode;
import com.silliconthink.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException ex) {
        HttpStatus status = resolveHttpStatus(ex.getCode());
        return ResponseEntity.status(status).body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<Result<Void>> handleValidation(Exception ex) {
        String message = ErrorCode.BAD_REQUEST.getMessage();
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            message = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            message = be.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof ConstraintViolationException cve) {
            message = cve.getMessage();
        }
        return ResponseEntity.badRequest().body(Result.fail(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Result<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.fail(ErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.fail(ErrorCode.FORBIDDEN));
    }

    /** 并发下唯一键冲突时按索引名映射可读业务错误 */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        String msg = ex.getMostSpecificCause() != null
                ? String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase()
                : "";
        ErrorCode code = ErrorCode.CONFLICT;
        if (msg.contains("uk_blog_post_slug")) {
            code = ErrorCode.SLUG_EXISTS;
        } else if (msg.contains("uk_sys_user_username")) {
            code = ErrorCode.USERNAME_EXISTS;
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.fail(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.INTERNAL_ERROR));
    }

    private HttpStatus resolveHttpStatus(int code) {
        if (code == ErrorCode.UNAUTHORIZED.getCode()
                || code == ErrorCode.AUTH_FAILED.getCode()
                || code == ErrorCode.ACCOUNT_DISABLED.getCode()
                || code == ErrorCode.PASSWORD_LOGIN_UNSUPPORTED.getCode()
                || code == ErrorCode.OAUTH_STATE_INVALID.getCode()
                || code == ErrorCode.OAUTH_EXCHANGE_INVALID.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code == ErrorCode.FORBIDDEN.getCode()) {
            return HttpStatus.FORBIDDEN;
        }
        if (code == ErrorCode.NOT_FOUND.getCode()
                || code == ErrorCode.CONTENT_OBJECT_MISSING.getCode()) {
            return HttpStatus.NOT_FOUND;
        }
        if (code == ErrorCode.CONFLICT.getCode()
                || code == ErrorCode.USERNAME_EXISTS.getCode()
                || code == ErrorCode.SLUG_EXISTS.getCode()) {
            return HttpStatus.CONFLICT;
        }
        if (code == ErrorCode.OAUTH_NOT_CONFIGURED.getCode()
                || code == ErrorCode.MEDIA_STORAGE_UNAVAILABLE.getCode()) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (code >= 50000) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
