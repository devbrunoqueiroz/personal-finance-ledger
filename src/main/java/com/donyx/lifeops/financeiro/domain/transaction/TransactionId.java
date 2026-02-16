package com.donyx.lifeops.financeiro.domain.transaction;

import java.util.Objects;
import java.util.UUID;

public final class TransactionId {
    private final UUID value;

    private TransactionId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static TransactionId of(UUID uuid) {
        return new TransactionId(uuid);
    }

    public static TransactionId of(String id) {
        Objects.requireNonNull(id, "id");
        try {
            return new TransactionId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID string for TransactionId: " + id, ex);
        }
    }

    public static TransactionId random() {
        return new TransactionId(UUID.randomUUID());
    }

    public UUID asUuid() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionId that)) return false;
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
