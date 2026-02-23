package com.donyx.lifeops.financeiro.application.usecases.category;

import com.donyx.lifeops.financeiro.application.ports.category.CategoryRepository;
import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.util.List;
import java.util.UUID;

public class ListCategoriesUseCase {

    private final CategoryRepository repository;

    public ListCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> execute(UserId userId) {
        return repository.findAllByOwnerId(userId).stream().toList();
    }
}
