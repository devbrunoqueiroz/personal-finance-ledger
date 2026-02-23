package com.donyx.lifeops.financeiro.adapters.inbound.web.dto.category;

import com.donyx.lifeops.financeiro.domain.category.CategoryType;

import java.util.UUID;

public record CategoryListItemResponse(
        UUID id,
        String name,
        String description,
        CategoryType type
) {}