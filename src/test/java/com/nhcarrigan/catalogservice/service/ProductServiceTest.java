package com.nhcarrigan.catalogservice.service;

import com.nhcarrigan.catalogservice.dto.ProductRequest;
import com.nhcarrigan.catalogservice.entity.Product;
import com.nhcarrigan.catalogservice.exception.DuplicateSkuException;
import com.nhcarrigan.catalogservice.exception.InsufficientStockException;
import com.nhcarrigan.catalogservice.exception.ProductNotFoundException;
import com.nhcarrigan.catalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration-style tests for the stock-adjustment business logic, backed by
 * the in-memory H2 database configured in src/test/resources/application.yml.
 */
@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

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
    void adjustStockAllowsExactlyZero() {
        Product updated = productService.adjustStock(testProduct.getId(), -10);
        assertThat(updated.getStockQuantity()).isZero();
    }
}
