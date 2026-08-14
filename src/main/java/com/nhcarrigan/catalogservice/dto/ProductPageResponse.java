package com.nhcarrigan.catalogservice.dto;

import com.nhcarrigan.catalogservice.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;

public record ProductPageResponse(List<Product> content, long totalElements, int totalPages) {

  public static ProductPageResponse from(Page<Product> page) {
    return new ProductPageResponse(
        page.getContent(), page.getTotalElements(), page.getTotalPages());
  }
}
