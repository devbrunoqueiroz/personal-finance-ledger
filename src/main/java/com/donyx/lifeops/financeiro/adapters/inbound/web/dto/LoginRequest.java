package com.donyx.lifeops.financeiro.adapters.inbound.web.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull
        String email,
        @NotNull
        String password
) {}