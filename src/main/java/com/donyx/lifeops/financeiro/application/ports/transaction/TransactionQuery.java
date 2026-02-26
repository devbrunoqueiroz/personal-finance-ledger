package com.donyx.lifeops.financeiro.application.ports.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionQuery(
        String text,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        LocalDate dueFrom,
        LocalDate dueTo,
        LocalDate settledFrom,
        LocalDate settledTo,
        TransactionType type,
        TransactionStatus status,
        CategoryId categoryId
) {}