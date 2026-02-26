package com.donyx.lifeops.financeiro.application.ports.category;

import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findAllByOwnerId(UserId userId);
    void deleteById(CategoryId id);
    void deleteByIdAndOwnerId(CategoryId id, UserId ownerId);
    Optional<Category> findByIdAndOwnerId(CategoryId id, UserId ownerId);
}
