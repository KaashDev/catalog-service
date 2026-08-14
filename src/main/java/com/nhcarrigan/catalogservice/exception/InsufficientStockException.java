package com.nhcarrigan.catalogservice.exception;

public class InsufficientStockException extends RuntimeException {

  public InsufficientStockException(Long productId, int currentStock, int requestedDelta) {
    super(
        String.format(
            "Cannot apply stock change of %d to product %d: only %d in stock",
            requestedDelta, productId, currentStock));
  }
}
