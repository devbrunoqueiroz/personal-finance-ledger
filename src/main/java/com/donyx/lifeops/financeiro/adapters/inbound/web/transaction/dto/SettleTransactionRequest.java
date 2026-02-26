package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto;

import java.time.LocalDate;

public record SettleTransactionRequest(
        LocalDate settledAt
) {
}
