package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction;

import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.CreateTransactionRequest;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.CreateTransactionCommand;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.time.Instant;
import java.util.UUID;

public class TransactionInboundMapper {
    private TransactionInboundMapper() {}

    public static CreateTransactionCommand toCommand(CreateTransactionRequest request, UUID userId) {
        return new CreateTransactionCommand(
                UserId.of(userId),
                request.amount(),
                request.type(),
                request.description(),
                request.notes(),
                Instant.now(),
                request.dueDate(),
                request.settledAt(),
                CategoryId.of(request.categoryId()),
                request.recurring()
        );
    }
}
