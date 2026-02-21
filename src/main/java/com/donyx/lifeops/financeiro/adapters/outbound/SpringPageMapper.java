package com.donyx.lifeops.financeiro.adapters.outbound;

import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

public final class SpringPageMapper {
    private SpringPageMapper() {}

    public static Pageable toPageable(com.donyx.lifeops.financeiro.application.ports.common.PageRequest page) {
        // ajusta conforme seu PageRequest (pageNumber, pageSize, sort?)
        return PageRequest.of(page.page(), page.size());
    }

    public static <E, D> com.donyx.lifeops.financeiro.application.ports.common.PageResult<D> toPageResult(Page<E> page, Function<E, D> mapper) {
        List<D> items = page.getContent().stream()
                .map(mapper)
                .toList();

        return new PageResult<>(
                items,
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}