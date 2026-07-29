package com.vishakha.position_doctor_project.domain.marketdata.mapper;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import org.springframework.stereotype.Component;

/**
 * SymbolMapper converts application symbols and exchange codes into Finnhub-compatible ticker format.
 */
@Component
public class SymbolMapper {

    public String toFinnhubSymbol(String symbol, Exchange exchange) {
        if (symbol == null || symbol.isBlank()) {
            return "AAPL";
        }

        String cleanedSymbol = symbol.trim().toUpperCase();

        if (exchange == null) {
            return cleanedSymbol;
        }

        switch (exchange) {
            case NSE:
                if (!cleanedSymbol.endsWith(".NS")) {
                    return cleanedSymbol + ".NS";
                }
                return cleanedSymbol;

            case BSE:
                if (!cleanedSymbol.endsWith(".BO")) {
                    return cleanedSymbol + ".BO";
                }
                return cleanedSymbol;

            case NASDAQ:
            case NYSE:
            default:
                return cleanedSymbol;
        }
    }
}
