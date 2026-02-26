package com.donyx.lifeops.financeiro.application.usecases.transaction;


import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.CreateTransactionCommand;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;

import java.time.Instant;

public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public CreateTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction execute(
            CreateTransactionCommand command
    ) {

        var tx = Transaction.create(
                command.ownerId(),
                command.amount(),
                command.type(),
                command.createdAt() != null ? command.createdAt() : Instant.now(),
                command.recurring()
        );

        tx.setDescription(command.description());
        if (command.notes() != null) tx.setNotes(command.notes());

        if (command.dueDate() != null) tx.setDueDate(command.dueDate());

        if (command.categoryId() != null) tx.setCategoryId(command.categoryId());

        if (command.settledAt() != null) {
            tx.settle(command.settledAt());
        }

        return transactionRepository.save(tx);
    }
}
