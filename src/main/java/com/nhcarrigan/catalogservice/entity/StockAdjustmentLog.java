package com.nhcarrigan.catalogservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Records a successful stock adjustment for a product.
 *
 * <p>Includes a snapshot of the product's name and SKU at the time of the
 * adjustment, so each entry remains meaningful even after the product it
 * refers to has been deleted.
 */

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

  private String productName;

  private String productSku;

  protected StockAdjustmentLog() {
    // required by JPA
  }

  public StockAdjustmentLog(Long productId, Integer delta, Integer resultingQuantity, String productName, String productSku) {
    this.productId = productId;
    this.productName = productName;
    this.productSku = productSku;
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

  public String getProductName() {
    return productName;
  }

  public String getProductSku() {
    return productSku;
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
