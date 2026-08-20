package com.nhcarrigan.catalogservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nhcarrigan.catalogservice.dto.ProductRequest;
import com.nhcarrigan.catalogservice.entity.Product;
import com.nhcarrigan.catalogservice.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class ProductOptimisticLockingTest {

  @Autowired private ProductRepository productRepository;

  @Autowired private ProductService productService;

  @Autowired private TransactionTemplate transactionTemplate;

  @Test
  void staleVersionUpdateIsRejected() {
    Product product = createProduct();

    Product staleProduct =
        transactionTemplate.execute(
            status -> productRepository.findById(product.getId()).orElseThrow());

    assertThat(staleProduct).isNotNull();
    assertThat(staleProduct.getVersion()).isNotNull();

    Long staleVersion = staleProduct.getVersion();

    transactionTemplate.execute(
        status -> {
          productService.adjustStock(product.getId(), 5);
          return null;
        });

    assertThatThrownBy(
            () ->
                transactionTemplate.execute(
                    status -> {
                      staleProduct.setStockQuantity(20);
                      productRepository.save(staleProduct);
                      return null;
                    }))
        .isInstanceOf(ObjectOptimisticLockingFailureException.class);

    Product reloaded = productRepository.findById(product.getId()).orElseThrow();

    assertThat(reloaded.getStockQuantity()).isEqualTo(15);
    assertThat(reloaded.getVersion()).isGreaterThan(staleVersion);
  }

  private Product createProduct() {
    return transactionTemplate.execute(
        status -> {
          ProductRequest request = new ProductRequest();
          request.setName("Optimistic Lock Widget");
          request.setSku("OPT-LOCK-" + System.nanoTime());
          request.setCategory("Test Category");
          request.setPrice(new BigDecimal("9.99"));
          request.setStockQuantity(10);

          return productService.create(request);
        });
  }
}
