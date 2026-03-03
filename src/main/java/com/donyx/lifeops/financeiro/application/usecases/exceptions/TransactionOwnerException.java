package com.donyx.lifeops.financeiro.application.usecases.exceptions;

public class TransactionOwnerException extends RuntimeException {
    public TransactionOwnerException() {
        super("Transaction does not belong to user");
    }
}
