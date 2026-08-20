package com.nhcarrigan.catalogservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.INSTANT;

import com.nhcarrigan.catalogservice.dto.BulkStockAdjustmentRequest;
import com.nhcarrigan.catalogservice.dto.ProductRequest;
import com.nhcarrigan.catalogservice.entity.Product;
import com.nhcarrigan.catalogservice.entity.StockAdjustmentLog;
import com.nhcarrigan.catalogservice.exception.DuplicateSkuException;
import com.nhcarrigan.catalogservice.exception.InsufficientStockException;
import com.nhcarrigan.catalogservice.exception.ProductNotFoundException;
import com.nhcarrigan.catalogservice.repository.ProductRepository;
import com.nhcarrigan.catalogservice.repository.StockAdjustmentLogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration-style tests for the stock-adjustment business logic, backed by the in-memory H2
 * database configured in src/test/resources/application.yml.
 */
@SpringBootTest
@Transactional
class ProductServiceTest {

  @Autowired private ProductService productService;

  @Autowired private ProductRepository productRepository;

  @Autowired private StockAdjustmentLogRepository stockAdjustmentLogRepository;

  private Product testProduct;

  private Product createTestProduct(String sku, int stock) {
    ProductRequest request = new ProductRequest();
    request.setName("Test Widget");
    request.setSku(sku);
    request.setCategory("Test Category");
    request.setPrice(new BigDecimal("9.99"));
    request.setStockQuantity(stock);
    return productService.create(request);
  }

  @BeforeEach
  void setUp() {
    ProductRequest request = new ProductRequest();
    request.setName("Test Widget");
    request.setSku("TEST-SKU-" + System.nanoTime());
    request.setCategory("Test Category");
    request.setPrice(new BigDecimal("9.99"));
    request.setStockQuantity(10);
    testProduct = productService.create(request);
  }

  @Test
  void createPersistsProductWithGivenFields() {
    assertThat(testProduct.getId()).isNotNull();
    assertThat(testProduct.getName()).isEqualTo("Test Widget");
    assertThat(testProduct.getStockQuantity()).isEqualTo(10);
  }

  @Test
  void createRejectsDuplicateSku() {
    ProductRequest duplicate = new ProductRequest();
    duplicate.setName("Another Widget");
    duplicate.setSku(testProduct.getSku());
    duplicate.setCategory("Test Category");
    duplicate.setPrice(new BigDecimal("5.00"));
    duplicate.setStockQuantity(5);

    assertThatThrownBy(() -> productService.create(duplicate))
        .isInstanceOf(DuplicateSkuException.class);
  }

  @Test
  void findByIdThrowsWhenMissing() {
    assertThatThrownBy(() -> productService.findById(-1L))
        .isInstanceOf(ProductNotFoundException.class);
  }

  @Test
  void adjustStockIncrementsQuantity() {
    Product updated = productService.adjustStock(testProduct.getId(), 5);
    assertThat(updated.getStockQuantity()).isEqualTo(15);
  }

  @Test
  void adjustStockCreatesExactlyOneLog() {
    productService.adjustStock(testProduct.getId(), 5);

    List<StockAdjustmentLog> logs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(testProduct.getId());

    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getProductId()).isEqualTo(testProduct.getId());
    assertThat(logs.get(0).getDelta()).isEqualTo(5);
    assertThat(logs.get(0).getResultingQuantity()).isEqualTo(15);
    assertThat(logs.get(0).getTimestamp()).isNotNull();
  }

  @Test
  void adjustStockCreatesALogIncludingProductNameAndSKU() {
    productService.adjustStock(testProduct.getId(), 5);

    productService.delete(testProduct.getId());

    List<StockAdjustmentLog> logs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(testProduct.getId());

    assertThat(logs.get(0).getProductName()).isEqualTo(testProduct.getName());
    assertThat(logs.get(0).getProductSku()).isEqualTo(testProduct.getSku());
    assertThat(logs.get(0).getProductId()).isEqualTo(testProduct.getId());
    assertThat(logs.get(0).getDelta()).isEqualTo(5);
    assertThat(logs.get(0).getResultingQuantity()).isEqualTo(15);
    assertThat(logs.get(0).getTimestamp()).isNotNull();
    assertThat(productRepository.findById(testProduct.getId())).isEmpty();
  }

  @Test
  void adjustStockDecrementsQuantity() {
    Product updated = productService.adjustStock(testProduct.getId(), -4);
    assertThat(updated.getStockQuantity()).isEqualTo(6);
  }

  @Test
  void adjustStockRejectsDropBelowZero() {
    assertThatThrownBy(() -> productService.adjustStock(testProduct.getId(), -11))
        .isInstanceOf(InsufficientStockException.class);

    // stock must be unchanged after the rejected operation
    Product reloaded = productRepository.findById(testProduct.getId()).orElseThrow();
    assertThat(reloaded.getStockQuantity()).isEqualTo(10);
  }

  @Test
  void rejectedAdjustmentCreatesNoLog() {
    assertThatThrownBy(() -> productService.adjustStock(testProduct.getId(), -11))
            .isInstanceOf(InsufficientStockException.class);

    List<StockAdjustmentLog> logs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(testProduct.getId());

    assertThat(logs).isEmpty();
  }

  @Test
  void adjustStockAllowsExactlyZero() {
    Product updated = productService.adjustStock(testProduct.getId(), -10);
    assertThat(updated.getStockQuantity()).isZero();
  }

  @Test
  void bulkAdjustStockAppliesAllAdjustments() {
    Product secondProduct = createTestProduct("TEST-SKU-" + System.nanoTime(), 20);

    List<BulkStockAdjustmentRequest> adjustments =
        List.of(
            new BulkStockAdjustmentRequest(testProduct.getId(), 5),
            new BulkStockAdjustmentRequest(secondProduct.getId(), -4));

    List<Product> updated = productService.bulkAdjustStock(adjustments);

    assertThat(updated)
        .extracting(Product::getId)
        .containsExactly(testProduct.getId(), secondProduct.getId());

    assertThat(productRepository.findById(testProduct.getId()).orElseThrow().getStockQuantity())
        .isEqualTo(15);

    assertThat(productRepository.findById(secondProduct.getId()).orElseThrow().getStockQuantity())
        .isEqualTo(16);
  }

  @Test
  void bulkAdjustStockCreatesOneLogPerAdjustment() {
    Product secondProduct = createTestProduct("TEST-SKU-" + System.nanoTime(), 20);

    List<BulkStockAdjustmentRequest> adjustments =
            List.of(
                    new BulkStockAdjustmentRequest(testProduct.getId(), 5),
                    new BulkStockAdjustmentRequest(secondProduct.getId(), -4));

    productService.bulkAdjustStock(adjustments);

    List<StockAdjustmentLog> firstProductLogs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(testProduct.getId());

    List<StockAdjustmentLog> secondProductLogs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(secondProduct.getId());

    assertThat(firstProductLogs).hasSize(1);
    assertThat(firstProductLogs.get(0).getDelta()).isEqualTo(5);
    assertThat(firstProductLogs.get(0).getResultingQuantity()).isEqualTo(15);

    assertThat(secondProductLogs).hasSize(1);
    assertThat(secondProductLogs.get(0).getDelta()).isEqualTo(-4);
    assertThat(secondProductLogs.get(0).getResultingQuantity()).isEqualTo(16);
  }

  @Test
  void bulkAdjustStockAllowsExactlyZero() {
    List<BulkStockAdjustmentRequest> adjustments =
        List.of(new BulkStockAdjustmentRequest(testProduct.getId(), -10));

    List<Product> updated = productService.bulkAdjustStock(adjustments);

    assertThat(updated.get(0).getStockQuantity()).isZero();
  }

  @Test
  void bulkAdjustStockRejectsNegativeStock() {
    List<BulkStockAdjustmentRequest> adjustments =
        List.of(new BulkStockAdjustmentRequest(testProduct.getId(), -11));

    assertThatThrownBy(() -> productService.bulkAdjustStock(adjustments))
        .isInstanceOf(InsufficientStockException.class);

    Product reloaded = productRepository.findById(testProduct.getId()).orElseThrow();

    assertThat(reloaded.getStockQuantity()).isEqualTo(10);
  }

  @Test
  void bulkAdjustStockRollsBackEntireBatchWhenOneAdjustmentFails() {
    Product secondProduct = createTestProduct("TEST-SKU-" + System.nanoTime(), 10);

    List<BulkStockAdjustmentRequest> adjustments =
        List.of(
            new BulkStockAdjustmentRequest(testProduct.getId(), 5),
            new BulkStockAdjustmentRequest(secondProduct.getId(), -11));

    assertThatThrownBy(() -> productService.bulkAdjustStock(adjustments))
        .isInstanceOf(InsufficientStockException.class);

    Product firstReloaded = productRepository.findById(testProduct.getId()).orElseThrow();

    Product secondReloaded = productRepository.findById(secondProduct.getId()).orElseThrow();

    assertThat(firstReloaded.getStockQuantity()).isEqualTo(10);
    assertThat(secondReloaded.getStockQuantity()).isEqualTo(10);
  }

  @Test
  void bulkAdjustStockRejectsMissingProduct() {
    List<BulkStockAdjustmentRequest> adjustments = List.of(new BulkStockAdjustmentRequest(-1L, 5));

    assertThatThrownBy(() -> productService.bulkAdjustStock(adjustments))
        .isInstanceOf(ProductNotFoundException.class);
  }

  @Test
  void bulkAdjustStockAppliesRepeatedAdjustmentsSequentially() {
    List<BulkStockAdjustmentRequest> adjustments =
        List.of(
            new BulkStockAdjustmentRequest(testProduct.getId(), 5),
            new BulkStockAdjustmentRequest(testProduct.getId(), -3));

    List<Product> updated = productService.bulkAdjustStock(adjustments);

    assertThat(updated).hasSize(1).first().extracting(Product::getStockQuantity).isEqualTo(12);
  }

  @Test
  void bulkAdjustStockCreatesLogForEachRepeatedAdjustment() {
    List<BulkStockAdjustmentRequest> adjustments =
            List.of(
                    new BulkStockAdjustmentRequest(testProduct.getId(), 5),
                    new BulkStockAdjustmentRequest(testProduct.getId(), -3));

    productService.bulkAdjustStock(adjustments);

    List<StockAdjustmentLog> logs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(testProduct.getId());

    assertThat(logs).hasSize(2);

    assertThat(logs.get(0).getDelta()).isEqualTo(-3);
    assertThat(logs.get(0).getResultingQuantity()).isEqualTo(12);

    assertThat(logs.get(1).getDelta()).isEqualTo(5);
    assertThat(logs.get(1).getResultingQuantity()).isEqualTo(15);
  }

  @Test
  void getStockHistoryBreaksTiedTimestampsByIdDescending() {
    StockAdjustmentLog older = new StockAdjustmentLog(testProduct.getId(), 5, 15, testProduct.getName(), testProduct.getSku());
    StockAdjustmentLog newer = new StockAdjustmentLog(testProduct.getId(), -3, 12, testProduct.getName(), testProduct.getSku());
    Instant timestamp1 = Instant.now();

    ReflectionTestUtils.setField(older, "timestamp", timestamp1);
    ReflectionTestUtils.setField(newer, "timestamp", timestamp1);

    stockAdjustmentLogRepository.save(older);
    stockAdjustmentLogRepository.save(newer);


    List<StockAdjustmentLog> logs =
            stockAdjustmentLogRepository.findByProductIdOrderByTimestampDescIdDesc(testProduct.getId());

    assertThat(logs.get(0).getResultingQuantity()).isEqualTo(12);
    assertThat(logs.get(0).getTimestamp()).isEqualTo(timestamp1);
    assertThat(logs.get(1).getResultingQuantity()).isEqualTo(15);
    assertThat(logs.get(1).getTimestamp()).isEqualTo(timestamp1);

  }
}
