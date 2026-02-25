package com.donyx.lifeops.financeiro.application.usecases.transaction;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public class SearchTransactionsUseCase {

    private final TransactionRepository repository;

    public SearchTransactionsUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public PageResult<Transaction> execute(
            UserId ownerId,
            TransactionQuery query,
            Pagination pageRequest
    ) {

        if (ownerId == null) {
            throw new IllegalArgumentException("OwnerId cannot be null");
        }

        if (pageRequest.page() < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if (pageRequest.size() <= 0) {
            throw new IllegalArgumentException("Size must be greater than zero");
        }

        return repository.search(ownerId, query, pageRequest);
    }
}
