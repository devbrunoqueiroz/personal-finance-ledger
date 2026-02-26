package com.donyx.lifeops.financeiro.application.usecases.category.command;

import com.donyx.lifeops.financeiro.domain.category.CategoryType;

import java.util.UUID;

public record CreateCategoryCommand(UUID userId, String name, String description, CategoryType type) {}