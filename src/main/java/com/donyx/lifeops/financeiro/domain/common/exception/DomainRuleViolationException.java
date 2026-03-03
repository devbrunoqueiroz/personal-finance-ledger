package com.donyx.lifeops.financeiro.domain.common.exception;

public class DomainRuleViolationException extends DomainException{
    public DomainRuleViolationException(String message) {
        super(message);
    }
}
