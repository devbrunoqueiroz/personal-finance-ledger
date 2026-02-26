package com.donyx.lifeops.financeiro.application.usecases.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.UpdateTransactionCommand;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;

public class UpdateTransactionUsecase {

    private final TransactionRepository transactionRepository;

    public UpdateTransactionUsecase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction execute(UpdateTransactionCommand command) {
        var tx = transactionRepository.findById(command.transactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (command.amount() != null) tx.changeAmount(command.amount());
        if (command.description() != null) tx.setDescription(command.description());
        if (command.notes() != null) tx.setNotes(command.notes());
        if (command.dueDate() != null) tx.setDueDate(command.dueDate());
        if (command.categoryId() != null) tx.setCategoryId(command.categoryId());

        return transactionRepository.save(tx);
    }

}
