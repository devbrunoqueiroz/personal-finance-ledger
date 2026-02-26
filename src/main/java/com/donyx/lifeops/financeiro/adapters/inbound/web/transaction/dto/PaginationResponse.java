package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto;

import com.donyx.lifeops.financeiro.application.ports.common.PageResult;

import java.util.List;

public record PaginationResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PaginationResponse<T> from(PageResult<T> p) {
        return new PaginationResponse<>(p.content(), p.page(), p.size(), p.totalElements(), p.totalPages());
    }
}
