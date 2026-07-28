package com.vishakha.position_doctor_project.common.exception;

import lombok.Getter;

/**
 * Root custom runtime exception for Position Doctor domain.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    protected BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
