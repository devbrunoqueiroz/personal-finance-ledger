package com.donyx.lifeops.financeiro.adapters.inbound.web.dto.category;

import com.donyx.lifeops.financeiro.domain.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotNull CategoryType type
) {}