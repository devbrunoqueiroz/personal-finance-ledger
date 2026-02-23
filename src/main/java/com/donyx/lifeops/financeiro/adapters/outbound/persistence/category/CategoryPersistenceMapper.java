package com.donyx.lifeops.financeiro.adapters.outbound.persistence.category;

import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.util.Objects;
import java.util.UUID;

public class CategoryPersistenceMapper {
    private CategoryPersistenceMapper() {}

    public static JpaCategoryEntity toEntity(Category domain) {
        Objects.requireNonNull(domain, "domain category cannot be null");

        JpaCategoryEntity e = new JpaCategoryEntity();
        e.setId(domain.id().asUuid());
        e.setOwnerId(domain.userId().asUuid());
        e.setName(domain.name());
        e.setDescription(domain.description());
        e.setType(domain.type());
        e.setCreatedAt(domain.createdAt());
        e.setUpdatedAt(domain.updatedAt());
        return e;
    }

    public static Category toDomain(JpaCategoryEntity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");

        return Category.reconstitute(
                CategoryId.of(entity.getId()),
                UserId.of(entity.getOwnerId()),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static void applyUpdates(Category domain, JpaCategoryEntity target) {
        Objects.requireNonNull(domain, "domain category cannot be null");
        Objects.requireNonNull(target, "target entity cannot be null");

        UUID domainId = domain.id().asUuid();
        if (!domainId.equals(target.getId())) {
            throw new IllegalArgumentException("Cannot apply updates: different category ids");
        }

        target.setName(domain.name());
        target.setDescription(domain.description());
    }
}
