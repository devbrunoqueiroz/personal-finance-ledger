package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQueryPort;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.UUID;

public class TransactionQueryAdapter implements TransactionQueryPort {

    private final TransactionJpaRepository repository;

    public TransactionQueryAdapter(TransactionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Transaction> find(UserId ownerId,
                                        TransactionQuery query,
                                        Pagination pagination) {

        UUID ownerUuid = ownerId.asUuid();

        var spec = TransactionSpecifications.byUserAndQuery(ownerUuid, query);

        Pageable pageable = toPageable(pagination);

        Page<JpaTransactionEntity> page = repository.findAll(spec, pageable);

        return new PageResult<>(
                page.getContent()
                        .stream()
                        .map(TransactionPersistenceMapper::toDomain)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Pageable toPageable(Pagination request) {

        Sort sort = Sort.unsorted();

        if (request.sorts() != null && !request.sorts().isEmpty()) {

            sort = Sort.by(
                    request.sorts().stream()
                            .map(s -> new Sort.Order(
                                    s.direction() == Pagination.Sort.Direction.DESC
                                            ? Sort.Direction.DESC
                                            : Sort.Direction.ASC,
                                    s.field()
                            ))
                            .toList()
            );
        }

        return PageRequest.of(
                request.page(),
                request.size(),
                sort
        );
    }
}