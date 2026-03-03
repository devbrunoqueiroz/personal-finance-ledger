package com.donyx.lifeops.financeiro.application.usecases.exceptions;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException() {
        super("Transaction not found.");
    }
}
