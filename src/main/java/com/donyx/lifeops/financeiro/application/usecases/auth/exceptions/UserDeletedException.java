package com.donyx.lifeops.financeiro.application.usecases.auth.exceptions;

public class UserDeletedException extends RuntimeException {
    public UserDeletedException() {
        super("User is deleted");
    }
}