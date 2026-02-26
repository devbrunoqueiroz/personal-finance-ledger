package com.donyx.lifeops.financeiro.application.usecases.transaction.command;


import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTransactionCommand(

        TransactionId transactionId,
        UserId ownerId,

        String description,
        String notes,
        BigDecimal amount,
        TransactionType type,
        LocalDate dueDate,
        CategoryId categoryId,
        Boolean recurring

) {
}