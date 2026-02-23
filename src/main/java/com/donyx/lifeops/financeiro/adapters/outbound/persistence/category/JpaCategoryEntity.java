package com.donyx.lifeops.financeiro.adapters.outbound.persistence.category;


import com.donyx.lifeops.financeiro.domain.category.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "ix_categories_owner_id", columnList = "owner_id"),
                @Index(name = "ix_categories_type", columnList = "type"),
                @Index(name = "ux_categories_owner_type_name", columnList = "owner_id,type,name", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
public class JpaCategoryEntity {

    @Id
    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 30)
    private CategoryType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}