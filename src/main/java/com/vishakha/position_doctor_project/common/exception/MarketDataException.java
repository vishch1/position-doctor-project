package com.vishakha.position_doctor_project.common.exception;

public class MarketDataException extends BaseException {

    public MarketDataException(String message) {
        super(message, "MARKET_DATA_FETCH_FAILED");
    }

    public MarketDataException(String message, Throwable cause) {
        super(message, cause, "MARKET_DATA_FETCH_FAILED");
    }
}
