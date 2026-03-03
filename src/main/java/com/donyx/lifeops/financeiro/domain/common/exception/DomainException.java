package com.donyx.lifeops.financeiro.domain.common.exception;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
}