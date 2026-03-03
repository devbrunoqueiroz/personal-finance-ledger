package com.donyx.lifeops.financeiro.domain.transaction.exception;

import com.donyx.lifeops.financeiro.domain.common.exception.DomainException;

public final class TransactionAlreadySettledException extends DomainException {
    public TransactionAlreadySettledException() {
        super("Transaction is already settled.");
    }
}