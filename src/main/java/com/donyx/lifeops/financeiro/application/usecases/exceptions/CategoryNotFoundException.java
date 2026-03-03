package com.donyx.lifeops.financeiro.application.usecases.exceptions;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super("Category not found.");
    }
}
