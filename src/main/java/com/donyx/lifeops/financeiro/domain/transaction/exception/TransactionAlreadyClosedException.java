package com.donyx.lifeops.financeiro.domain.transaction.exception;

import com.donyx.lifeops.financeiro.domain.common.exception.DomainException;

public class TransactionAlreadyClosedException extends DomainException {
    public TransactionAlreadyClosedException() {
        super("Cannot settle a failed transaction. Reopen it first.");
    }
}
