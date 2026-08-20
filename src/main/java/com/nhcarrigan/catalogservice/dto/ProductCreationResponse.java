package com.nhcarrigan.catalogservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nhcarrigan.catalogservice.entity.Product;

/**
 * Wrapper response DTO for product creation. Annotated with <code>
 * @JsonInclude(JsonInclude.Include.NON_NULL)</code> such that a when the warning string is <code>
 * null</code>, it isn't included in the serialized JSON.
 *
 * @param product The product that was just created
 * @param warning A warning string (leave <code>null</code> to exclude <code>"warning"</code> key
 *     from JSON)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductCreationResponse(@JsonUnwrapped Product product, String warning) {}
