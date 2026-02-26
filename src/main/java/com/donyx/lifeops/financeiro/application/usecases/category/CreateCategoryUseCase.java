package com.donyx.lifeops.financeiro.application.usecases.category;

import com.donyx.lifeops.financeiro.application.ports.category.CategoryRepository;
import com.donyx.lifeops.financeiro.application.usecases.category.command.CreateCategoryCommand;
import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.user.UserId;


public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(CreateCategoryCommand command) {
        Category category = Category.createNew(UserId.of(command.userId()), command.name(), command.description(), command.type());
        return categoryRepository.save(category);
    }

}
