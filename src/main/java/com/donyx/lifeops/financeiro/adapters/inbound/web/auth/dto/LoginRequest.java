package com.donyx.lifeops.financeiro.adapters.inbound.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email
        String email,
        @NotBlank
        String password
) {}