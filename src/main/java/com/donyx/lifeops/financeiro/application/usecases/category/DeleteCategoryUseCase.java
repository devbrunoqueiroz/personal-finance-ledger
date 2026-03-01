package com.donyx.lifeops.financeiro.application.usecases.category;

import com.donyx.lifeops.financeiro.application.ports.category.CategoryRepository;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void execute(CategoryId categoryId, UserId ownerId) {
        categoryRepository.deleteByIdAndOwnerId(categoryId, ownerId);
    }
}
