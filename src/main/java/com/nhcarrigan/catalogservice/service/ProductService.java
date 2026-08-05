package com.nhcarrigan.catalogservice.service;

import com.nhcarrigan.catalogservice.dto.ProductRequest;
import com.nhcarrigan.catalogservice.entity.Product;
import com.nhcarrigan.catalogservice.exception.DuplicateSkuException;
import com.nhcarrigan.catalogservice.exception.InsufficientStockException;
import com.nhcarrigan.catalogservice.exception.ProductNotFoundException;
import com.nhcarrigan.catalogservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Product create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }
        Product product = new Product(
                request.getName(),
                request.getSku(),
                request.getCategory(),
                request.getPrice(),
                request.getStockQuantity());
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product existing = findById(id);

        if (!existing.getSku().equalsIgnoreCase(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        existing.setName(request.getName());
        existing.setSku(request.getSku());
        existing.setCategory(request.getCategory());
        existing.setPrice(request.getPrice());
        existing.setStockQuantity(request.getStockQuantity());
        return productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Product existing = findById(id);
        productRepository.delete(existing);
    }

    /**
     * Applies a signed delta to a product's stock quantity. Positive deltas
     * restock, negative deltas draw down stock. The operation is rejected
     * (no partial writes) if it would take stock below zero.
     */
    @Transactional
    public Product adjustStock(Long id, int delta) {
        Product product = findById(id);
        int newQuantity = product.getStockQuantity() + delta;
        if (newQuantity < 0) {
            throw new InsufficientStockException(id, product.getStockQuantity(), delta);
        }
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }
}
