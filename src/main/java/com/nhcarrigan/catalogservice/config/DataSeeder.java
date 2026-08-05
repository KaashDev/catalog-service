package com.nhcarrigan.catalogservice.config;

import com.nhcarrigan.catalogservice.entity.Product;
import com.nhcarrigan.catalogservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Seeds a handful of sample products on startup so the API has something to
 * return out of the box. Only runs if the products table is empty, so it's
 * safe across restarts.
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            productRepository.save(new Product(
                    "Mechanical Keyboard", "SKU-1001", "Electronics", new BigDecimal("89.99"), 42));
            productRepository.save(new Product(
                    "Ergonomic Mouse", "SKU-1002", "Electronics", new BigDecimal("34.50"), 75));
            productRepository.save(new Product(
                    "USB-C Hub", "SKU-1003", "Electronics", new BigDecimal("24.99"), 120));
            productRepository.save(new Product(
                    "Notebook (Dot Grid)", "SKU-2001", "Office Supplies", new BigDecimal("6.25"), 200));
            productRepository.save(new Product(
                    "Fountain Pen", "SKU-2002", "Office Supplies", new BigDecimal("18.00"), 60));
            productRepository.save(new Product(
                    "Standing Desk Mat", "SKU-3001", "Furniture", new BigDecimal("45.00"), 15));
            productRepository.save(new Product(
                    "Monitor Arm", "SKU-3002", "Furniture", new BigDecimal("59.99"), 0));
        };
    }
}
