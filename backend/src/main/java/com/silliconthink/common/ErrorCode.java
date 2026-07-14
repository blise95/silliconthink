package com.silliconthink.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(40000, "bad request"),
    UNAUTHORIZED(40100, "unauthorized"),
    FORBIDDEN(40300, "forbidden"),
    NOT_FOUND(40400, "not found"),
    CONFLICT(40900, "conflict"),
    AUTH_FAILED(40101, "invalid username or password"),
    ACCOUNT_DISABLED(40102, "account disabled"),
    PASSWORD_LOGIN_UNSUPPORTED(40103, "password login is not available for this account"),
    USERNAME_EXISTS(40901, "username already exists"),
    WEAK_PASSWORD(40001, "password does not meet strength requirements"),
    INVALID_USERNAME(40002, "username is invalid"),
    OAUTH_STATE_INVALID(40110, "invalid oauth state"),
    OAUTH_EXCHANGE_INVALID(40111, "invalid or expired oauth code"),
    OAUTH_PROVIDER_ERROR(50201, "oauth provider error"),
    OAUTH_NOT_CONFIGURED(50301, "oauth is not configured"),
    INTERNAL_ERROR(50000, "internal server error");

    private final int code;
    private final String message;
}
