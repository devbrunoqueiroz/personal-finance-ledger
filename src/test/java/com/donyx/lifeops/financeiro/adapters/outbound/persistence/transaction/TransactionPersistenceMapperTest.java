package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionPersistenceMapperTest {

    @Test
    void roundTrip_preservesAllFields() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

        Transaction original = Transaction.hydrate(
                TransactionId.of(id),
                UserId.of(ownerId),
                "Pizza",
                "noite",
                new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                TransactionStatus.PENDING,
                LocalDate.of(2026, 1, 10),
                null,
                CategoryId.of(categoryId),
                createdAt,
                updatedAt
        );

        JpaTransactionEntity entity =
                TransactionPersistenceMapper.toEntity(original);

        Transaction restored =
                TransactionPersistenceMapper.toDomain(entity);

        assertEquals(id, restored.id().asUuid());
        assertEquals(ownerId, restored.ownerId().asUuid());
        assertEquals("Pizza", restored.description());
        assertEquals("noite", restored.notes());
        assertEquals(new BigDecimal("50.00"), restored.amount());
        assertEquals(TransactionType.EXPENSE, restored.type());
        assertEquals(TransactionStatus.PENDING, restored.status());
        assertEquals(LocalDate.of(2026, 1, 10), restored.dueDate());
        assertNull(restored.settledAt());
        assertEquals(categoryId, restored.categoryId().asUuid());
        assertEquals(createdAt, restored.createdAt());
        assertEquals(updatedAt, restored.updatedAt());
    }

    @Test
    void toDomain_handlesNullCategory() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.now();

        JpaTransactionEntity entity = new JpaTransactionEntity();
        entity.setId(id);
        entity.setOwnerId(ownerId);
        entity.setDescription("Test");
        entity.setNotes(null);
        entity.setAmount(new BigDecimal("10"));
        entity.setType(TransactionType.EXPENSE);
        entity.setStatus(TransactionStatus.PENDING);
        entity.setDueDate(LocalDate.now());
        entity.setSettledAt(null);
        entity.setCategoryId(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        Transaction tx = TransactionPersistenceMapper.toDomain(entity);

        assertNull(tx.categoryId());
    }

    @Test
    void toEntity_setsUpdatedAtWhenNull() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        Transaction tx = Transaction.hydrate(
                TransactionId.of(id),
                UserId.of(ownerId),
                "Test",
                null,
                new BigDecimal("10"),
                TransactionType.EXPENSE,
                TransactionStatus.PENDING,
                LocalDate.now(),
                null,
                null,
                createdAt,
                null // <- updatedAt null
        );

        JpaTransactionEntity entity =
                TransactionPersistenceMapper.toEntity(tx);

        assertEquals(createdAt, entity.getUpdatedAt());
    }
}