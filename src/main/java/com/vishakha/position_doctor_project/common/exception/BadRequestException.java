package com.vishakha.position_doctor_project.common.exception;

public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message, "BAD_REQUEST");
    }

    public BadRequestException(String message, String errorCode) {
        super(message, errorCode);
    }
}
