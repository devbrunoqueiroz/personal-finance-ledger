package com.donyx.lifeops.financeiro.adapters.inbound.web.dto;

public record CreateUserRequest(
    String name,
    String email,
    String password
) { }