package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto;

import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(

        @Size(max = 255)
        String description,

        @Size(max = 1000)
        String notes,

        @DecimalMin(value = "0.01")
        BigDecimal amount,

        TransactionType type,

        LocalDate dueDate,

        UUID categoryId,

        Boolean recurring

) {
}