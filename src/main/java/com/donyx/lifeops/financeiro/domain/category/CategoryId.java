package com.donyx.lifeops.financeiro.domain.category;

import java.util.Objects;
import java.util.UUID;

public final class CategoryId {
    private final UUID value;

    private CategoryId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static CategoryId of(UUID uuid) {
        return new CategoryId(uuid);
    }

    public static CategoryId of(String id) {
        Objects.requireNonNull(id, "id");
        try {
            return new CategoryId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID string for CategoryId: " + id, ex);
        }
    }

    public static CategoryId random() {
        return new CategoryId(UUID.randomUUID());
    }

    public UUID asUuid() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
