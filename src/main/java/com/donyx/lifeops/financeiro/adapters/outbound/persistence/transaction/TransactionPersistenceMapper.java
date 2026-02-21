package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public class TransactionPersistenceMapper {
    private TransactionPersistenceMapper() {
        /* This utility class should not be instantiated */
    }


    public static Transaction toDomain(JpaTransactionEntity entity) {
        if (entity == null) return null;

        return Transaction.hydrate(
                TransactionId.of(entity.getId()),
                UserId.of(entity.getOwnerId()),
                entity.getDescription(),
                entity.getNotes(),
                entity.getAmount(),
                entity.getType(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getSettledAt(),
                entity.getCategoryId() != null ? CategoryId.of(entity.getCategoryId()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static JpaTransactionEntity toEntity(Transaction transaction) {
        if (transaction == null) return null;

        JpaTransactionEntity entity = new JpaTransactionEntity();
        entity.setId(transaction.id().asUuid());
        entity.setOwnerId(transaction.ownerId().asUuid());

        entity.setDescription(transaction.description());
        entity.setNotes(transaction.notes());
        entity.setAmount(transaction.amount());
        entity.setType(transaction.type());
        entity.setStatus(transaction.status());

        entity.setDueDate(transaction.dueDate());
        entity.setSettledAt(transaction.settledAt());

        entity.setCategoryId(transaction.categoryId() != null ? transaction.categoryId().asUuid() : null);

        entity.setCreatedAt(transaction.createdAt());
        entity.setUpdatedAt(transaction.updatedAt() != null ? transaction.updatedAt() : transaction.createdAt());

        return entity;
    }
}