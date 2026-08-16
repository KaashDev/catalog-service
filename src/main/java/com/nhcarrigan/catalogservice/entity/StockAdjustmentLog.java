package com.nhcarrigan.catalogservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Records a successful stock adjustment for a product. */
@Entity
@Table(name = "stock_adjustment_logs")
public class StockAdjustmentLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long productId;

  private Integer delta;

  private Integer resultingQuantity;

  private Instant timestamp;

  protected StockAdjustmentLog() {
    // required by JPA
  }

  public StockAdjustmentLog(Long productId, Integer delta, Integer resultingQuantity) {
    this.productId = productId;
    this.delta = delta;
    this.resultingQuantity = resultingQuantity;
    this.timestamp = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Long getProductId() {
    return productId;
  }

  public Integer getDelta() {
    return delta;
  }

  public Integer getResultingQuantity() {
    return resultingQuantity;
  }

  public Instant getTimestamp() {
    return timestamp;
  }
}
