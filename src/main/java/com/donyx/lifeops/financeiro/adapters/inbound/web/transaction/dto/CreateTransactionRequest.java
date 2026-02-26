package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto;


import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull TransactionType type,

        @NotBlank String description,
        String notes,

        LocalDate dueDate,
        LocalDate settledAt,

        UUID categoryId,
        Boolean recurring
) {}