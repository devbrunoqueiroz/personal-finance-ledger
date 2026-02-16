package com.donyx.lifeops.financeiro.domain.category;

import com.donyx.lifeops.financeiro.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;

public class Category {
    private final CategoryId id;
    private final UserId userId;
    private String name;
    private String description;
    private final CategoryType type;
    private final Instant createdAt;
    private Instant updatedAt;

    public Category(CategoryId id, UserId userId, String name, String description, CategoryType type, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "CategoryId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.type = Objects.requireNonNull(type, "CategoryType cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = createdAt;
    }

    private String validateName(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        return name;
    }

    private String validateDescription(String description) {
        if (description != null && description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        return description;
    }

    public CategoryId id() { return id; }
    public UserId userId() { return userId; }
    public String name() { return name; }
    public String description() { return description; }
    public CategoryType type() { return type; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public void setName(String name) {
        this.name = validateName(name);
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        this.description = validateDescription(description);
        this.updatedAt = Instant.now();
    }
}
