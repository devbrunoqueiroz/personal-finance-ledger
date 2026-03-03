package com.donyx.lifeops.financeiro.application.usecases.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.application.usecases.exceptions.TransactionNotFoundException;
import com.donyx.lifeops.financeiro.application.usecases.exceptions.TransactionOwnerException;
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
                .orElseThrow(TransactionNotFoundException::new);

        if (!tx.ownerId().equals(userId)) {
            throw new TransactionOwnerException();
        }

        tx.settle(settledAt);
        transactionRepository.save(tx);
    }

}
