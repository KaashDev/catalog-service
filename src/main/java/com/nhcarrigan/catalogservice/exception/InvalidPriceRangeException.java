package com.nhcarrigan.catalogservice.exception;

import java.math.BigDecimal;

public class InvalidPriceRangeException extends RuntimeException {
    public InvalidPriceRangeException(BigDecimal minPrice, BigDecimal maxPrice) {
        super(String.format("Cannot filter using reversed price range: minimum %s is greater than maximum %s", minPrice, maxPrice));
    }
}
