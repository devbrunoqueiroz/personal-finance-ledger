package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto;

import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID ownerId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String description,
        String notes,
        LocalDate dueDate,
        LocalDate settledAt,
        UUID categoryId,
        boolean recurring,
        Instant createdAt,
        Instant updatedAt
) {

    public static TransactionResponse fromDomain(Transaction tx){
        return new TransactionResponse(
                tx.id().asUuid(),
                tx.ownerId().asUuid(),
                tx.amount(),
                tx.type(),
                tx.status(),
                tx.description(),
                tx.notes(),
                tx.dueDate(),
                tx.settledAt(),
                tx.categoryId() != null ? tx.categoryId().asUuid() : null,
                tx.recurring(),
                tx.createdAt(),
                Instant.now()
        );
    }
}