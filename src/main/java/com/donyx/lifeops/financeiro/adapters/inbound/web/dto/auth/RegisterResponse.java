package com.donyx.lifeops.financeiro.adapters.inbound.web.dto.auth;

public record RegisterResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

}
