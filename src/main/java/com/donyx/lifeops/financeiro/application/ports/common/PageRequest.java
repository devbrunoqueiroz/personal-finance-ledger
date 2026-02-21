package com.donyx.lifeops.financeiro.application.ports.common;

public record PageRequest(int page, int size, Sort sort) {
    public record Sort(String field, Direction direction) {
        public enum Direction { ASC, DESC }
    }
}