package com.donyx.lifeops.financeiro.adapters.inbound.web.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}