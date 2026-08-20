package com.nhcarrigan.catalogservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhcarrigan.catalogservice.dto.BulkStockAdjustmentRequest;
import com.nhcarrigan.catalogservice.dto.ProductRequest;
import com.nhcarrigan.catalogservice.entity.Product;
import com.nhcarrigan.catalogservice.exception.ProductNotFoundException;
import com.nhcarrigan.catalogservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
class ProductCachingTest {

  @Autowired private ProductService productService;

  @Autowired private ProductRepository productRepository;

  @Autowired private CacheManager cacheManager;

  @SpyBean private ProductRepository productRepositorySpy;

  private Product product;

  @BeforeEach
  void setUp() {
    cacheManager.getCache("products").clear();
    cacheManager.getCache("product").clear();
    productRepository.deleteAll();
    product =
        productRepository.save(
            new Product(
                "Cache Test Product",
                "CACHE-" + System.nanoTime(),
                "Test",
                new BigDecimal("10.00"),
                10,
                "Used to verify product caching."));
  }

  @Test
  void repeatedProductListReadsUseTheCache() {
    Pageable pageable = PageRequest.of(0, 20);

    productService.filterByPrice(null, null, pageable);
    productService.filterByPrice(null, null, pageable);

    verify(productRepositorySpy, times(1)).findAll(pageable);
  }

  @Test
  void repeatedProductReadsUseTheCache() {
    productService.findById(product.getId());
    productService.findById(product.getId());

    verify(productRepositorySpy, times(1)).findById(product.getId());
  }

  @Test
  void createEvictsTheProductListCache() {
    Pageable pageable = PageRequest.of(0, 20);
    productService.filterByPrice(null, null, pageable);

    productService.create(request("Created after cache", "CREATE-" + System.nanoTime(), 5));
    assertThat(productService.filterByPrice(null, null, pageable).getContent())
        .extracting(Product::getName)
        .contains("Created after cache");

    verify(productRepositorySpy, times(2)).findAll(pageable);
  }

  @Test
  void updateEvictsTheProductCache() {
    productService.findById(product.getId());

    productService.update(
        product.getId(), request("Updated product", product.getSku(), product.getStockQuantity()));

    assertThat(productService.findById(product.getId()).getName()).isEqualTo("Updated product");
    verify(productRepositorySpy, times(3)).findById(product.getId());
  }

  @Test
  void deleteEvictsTheProductCache() {
    productService.findById(product.getId());

    productService.delete(product.getId());

    assertThatThrownBy(() -> productService.findById(product.getId()))
        .isInstanceOf(ProductNotFoundException.class);
    verify(productRepositorySpy, times(3)).findById(product.getId());
  }

  @Test
  void stockAdjustmentEvictsTheProductCache() {
    productService.findById(product.getId());

    productService.adjustStock(product.getId(), 5);

    assertThat(productService.findById(product.getId()).getStockQuantity()).isEqualTo(15);
    verify(productRepositorySpy, times(3)).findById(product.getId());
  }

  @Test
  void bulkStockAdjustmentEvictsTheProductCache() {
    productService.findById(product.getId());

    productService.bulkAdjustStock(List.of(new BulkStockAdjustmentRequest(product.getId(), 5)));

    assertThat(productService.findById(product.getId()).getStockQuantity()).isEqualTo(15);
    verify(productRepositorySpy, times(3)).findById(product.getId());
  }

  private ProductRequest request(String name, String sku, int stock) {
    ProductRequest request = new ProductRequest();
    request.setName(name);
    request.setSku(sku);
    request.setCategory("Test");
    request.setPrice(new BigDecimal("10.00"));
    request.setStockQuantity(stock);
    request.setDescription("Cache test request");
    return request;
  }
}
