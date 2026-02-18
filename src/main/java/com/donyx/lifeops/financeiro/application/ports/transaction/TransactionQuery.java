package com.donyx.lifeops.financeiro.application.ports.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;

import java.time.LocalDate;

public record TransactionQuery(
        LocalDate fromInclusive,
        LocalDate toInclusive,
        CategoryId categoryId,
        TransactionType type // INCOME/EXPENSE ou null
) {}