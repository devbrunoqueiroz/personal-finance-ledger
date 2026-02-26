package com.donyx.lifeops.financeiro.adapters.inbound.web.category.dto;

import com.donyx.lifeops.financeiro.domain.category.CategoryType;

import java.time.Instant;
import java.util.UUID;
public record CategoryResponse(
        UUID id,
        UUID userId,
        String name,
        String description,
        CategoryType type,
        Instant createdAt,
        Instant updatedAt
) {}