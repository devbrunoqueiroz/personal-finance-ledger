package com.donyx.lifeops.financeiro.adapters.inbound.web.dto.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}