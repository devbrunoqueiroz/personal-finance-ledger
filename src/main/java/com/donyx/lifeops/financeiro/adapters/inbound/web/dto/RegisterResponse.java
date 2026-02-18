package com.donyx.lifeops.financeiro.adapters.inbound.web.dto;

public record RegisterResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

}
