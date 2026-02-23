package com.donyx.lifeops.financeiro.application.usecases.category;

import com.donyx.lifeops.financeiro.application.ports.category.CategoryRepository;
import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public class GetCategoryByIdUseCase {
    private final CategoryRepository categoryRepository;

    public GetCategoryByIdUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(CategoryId categoryId, UserId onwerId) {
        return categoryRepository.findByIdAndOwnerId(categoryId, onwerId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId.toString()));
    }
}
