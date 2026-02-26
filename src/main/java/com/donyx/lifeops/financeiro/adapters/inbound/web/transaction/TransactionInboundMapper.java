package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction;

import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.CreateTransactionRequest;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.UpdateTransactionRequest;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.CreateTransactionCommand;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.UpdateTransactionCommand;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.time.Instant;
import java.util.UUID;

public class TransactionInboundMapper {
    private TransactionInboundMapper() {}

    public static CreateTransactionCommand toCommand(CreateTransactionRequest request, UserId userId) {
        return new CreateTransactionCommand(
                userId,
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

    public static UpdateTransactionCommand toUpdateCommand(
            UUID transactionId,
            UpdateTransactionRequest request,
            UserId userId
    ) {

        return new UpdateTransactionCommand(
                TransactionId.of(transactionId),
                userId,
                request.description(),
                request.notes(),
                request.amount(),
                request.type(),
                request.dueDate(),
                request.categoryId() != null
                        ? CategoryId.of(request.categoryId())
                        : null,
                request.recurring()
        );
    }
}
