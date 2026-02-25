package com.donyx.lifeops.financeiro.application.ports.common;

import java.util.List;

public record Pagination(
        int page,
        int size,
        List<Sort> sorts
) {
    public record Sort(String field, Direction direction) {
        public enum Direction { ASC, DESC }
    }
}