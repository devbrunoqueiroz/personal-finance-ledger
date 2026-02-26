package com.donyx.lifeops.financeiro.application.usecases.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.time.LocalDate;

public class SettleTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public SettleTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void execute(TransactionId transactionId, UserId userId, LocalDate settledAt) {

        var tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!tx.ownerId().equals(userId)) {
            throw new IllegalStateException("Transaction does not belong to user");
        }

        tx.settle(settledAt);
        transactionRepository.save(tx);
    }

}
