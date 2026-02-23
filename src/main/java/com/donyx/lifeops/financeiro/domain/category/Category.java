package com.donyx.lifeops.financeiro.domain.category;

import com.donyx.lifeops.financeiro.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;

public class Category {
    private final CategoryId id;
    private final UserId ownerId;
    private String name;
    private String description;
    private final CategoryType type;
    private final Instant createdAt;
    private Instant updatedAt;

    public Category(CategoryId id, UserId ownerId, String name, String description, CategoryType type,
                    Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.type = Objects.requireNonNull(type);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Category reconstitute(CategoryId id, UserId userId, String name, String description,
                                        CategoryType type, Instant createdAt, Instant updatedAt) {
        return new Category(id, userId, name, description, type, createdAt, updatedAt);
    }

    public static Category createNew(UserId userId, String name, String description, CategoryType type) {
        Instant now = Instant.now();
        return new Category(CategoryId.random(), userId, name, description, type, now, now);
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
    public UserId userId() { return ownerId; }
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
