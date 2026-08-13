package com.nhcarrigan.catalogservice.exception;

public class InvalidSearchCriteriaException extends RuntimeException {

    public InvalidSearchCriteriaException() {
        super("Provide either a name or a category, not both");
    }
}