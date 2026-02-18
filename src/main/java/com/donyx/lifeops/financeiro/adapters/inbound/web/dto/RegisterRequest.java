package com.donyx.lifeops.financeiro.adapters.inbound.web.dto;

public record RegisterRequest(
        String name,
        String email,
        String password
) {
}
