package com.donyx.lifeops.financeiro.application.ports.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.PageRequest;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    PageResult<Transaction> findByUser(
            UserId userId,
            TransactionQuery query,
            PageRequest page
    );

    BigDecimal sumAmountByUserAndPeriod(
            UserId userId,
            LocalDate fromInclusive,
            LocalDate toInclusive
    );

}
