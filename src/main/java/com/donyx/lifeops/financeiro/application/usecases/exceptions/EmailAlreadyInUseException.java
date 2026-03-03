package com.donyx.lifeops.financeiro.application.usecases.exceptions;

public class EmailAlreadyInUseException extends RuntimeException{
    public EmailAlreadyInUseException(String email) {
        super("The email" + email + "is already in use.");
    }
}
