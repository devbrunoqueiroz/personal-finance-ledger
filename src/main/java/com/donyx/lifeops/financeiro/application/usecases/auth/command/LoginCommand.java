package com.donyx.lifeops.financeiro.application.usecases.auth.command;

public record LoginCommand(String email, String rawPassword) {}