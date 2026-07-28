package com.vishakha.position_doctor_project.common.exception;

public class RiskDiagnosticException extends BaseException {

    public RiskDiagnosticException(String message) {
        super(message, "RISK_DIAGNOSTIC_ERROR");
    }

    public RiskDiagnosticException(String message, Throwable cause) {
        super(message, cause, "RISK_DIAGNOSTIC_ERROR");
    }
}
