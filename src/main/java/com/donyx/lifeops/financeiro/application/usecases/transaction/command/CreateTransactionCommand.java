package com.donyx.lifeops.financeiro.application.usecases.transaction.command;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CreateTransactionCommand(
        UserId ownerId,
        BigDecimal amount,
        TransactionType type,
        String description,
        String notes,
        Instant createdAt,
        LocalDate dueDate,
        LocalDate settledAt,
        CategoryId categoryId,
        boolean recurring
) {}
