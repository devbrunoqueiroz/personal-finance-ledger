package com.donyx.lifeops.financeiro.domain.user;

import java.util.Objects;
import java.util.UUID;


public final class UserId {
    private final UUID value;

    private UserId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static UserId of(UUID uuid) {
        return new UserId(uuid);
    }

    public static UserId of(String id) {
        Objects.requireNonNull(id, "id");
        try {
            return new UserId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID string for UserId: " + id, ex);
        }
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }

    public UUID asUuid() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId userId)) return false;
        return value.equals(userId.value);
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
