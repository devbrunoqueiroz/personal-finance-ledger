package com.donyx.lifeops.financeiro.application.ports.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public interface TransactionQueryPort {
    PageResult<Transaction> find(UserId ownerId, TransactionQuery query, Pagination pagination);
}
