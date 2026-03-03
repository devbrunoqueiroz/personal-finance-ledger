package com.donyx.lifeops.financeiro.domain.transaction.exception;

import com.donyx.lifeops.financeiro.domain.common.exception.DomainException;

public class TransactionSettledException extends DomainException {
    public TransactionSettledException() {
        super("Cannot delete a settled transaction");
    }
}
