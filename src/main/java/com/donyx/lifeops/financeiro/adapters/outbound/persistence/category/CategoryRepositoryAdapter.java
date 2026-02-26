package com.donyx.lifeops.financeiro.adapters.outbound.persistence.category;

import com.donyx.lifeops.financeiro.application.ports.category.CategoryRepository;
import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository repository;

    public CategoryRepositoryAdapter(CategoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {
        JpaCategoryEntity entity = CategoryPersistenceMapper.toEntity(category);
        JpaCategoryEntity saved = repository.save(entity);
        return CategoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return repository.findById(id.asUuid())
                .map(CategoryPersistenceMapper::toDomain);
    }

    @Override
    public List<Category> findAllByOwnerId(UserId userId) {
        return repository.findAllByOwnerId(userId.asUuid())
                .stream().map(CategoryPersistenceMapper::toDomain).toList();
    }

    @Override
    public void deleteById(CategoryId id) {
        repository.deleteById(id.asUuid());
    }

    @Override
    public void deleteByIdAndOwnerId(CategoryId id, UserId ownerId) {
        long deleted = repository.deleteByIdAndOwnerId(id.asUuid(), ownerId.asUuid());
        if (deleted == 0) {
            throw new RuntimeException("Category not found or does not belong to the user");
        }
    }

    @Override
    public Optional<Category> findByIdAndOwnerId(CategoryId id, UserId ownerId) {
        return repository.findByIdAndOwnerId(id.asUuid(), ownerId.asUuid())
                .map(CategoryPersistenceMapper::toDomain);
    }
}
