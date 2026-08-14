package com.nhcarrigan.catalogservice.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Payload for adjusting a product's stock level.
 *
 * <p>{@code delta} may be positive (restock) or negative (sale/removal). The service layer rejects
 * any adjustment that would take stock below zero.
 */
public class StockAdjustmentRequest {

  @NotNull(message = "Delta is required")
  private Integer delta;

  public Integer getDelta() {
    return delta;
  }

  public void setDelta(Integer delta) {
    this.delta = delta;
  }
}
