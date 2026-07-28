package com.vishakha.position_doctor_project.common.exception;

public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode);
    }
}
