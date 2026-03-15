package com.atozshop.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String variantSku, Integer requested, Integer available) {
        super(String.format("Insufficient stock for variant %s. Requested: %d, Available: %d",
            variantSku, requested, available));
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
