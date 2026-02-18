package com.donyx.lifeops.financeiro.adapters.inbound.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 60) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
