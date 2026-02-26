package com.donyx.lifeops.financeiro.application.usecases.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public class CancelTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public CancelTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void execute(TransactionId id, UserId userId) {
        var tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (!tx.ownerId().equals(userId)) {
            throw new IllegalStateException("Transaction does not belong to user");
        }

        tx.cancel();
        transactionRepository.save(tx);
    }
}
