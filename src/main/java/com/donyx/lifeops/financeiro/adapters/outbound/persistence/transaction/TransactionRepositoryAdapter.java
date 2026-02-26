package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository transactionJpaRepository;

    public TransactionRepositoryAdapter(TransactionJpaRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }


    @Override
    public Transaction save(Transaction transaction) {
        JpaTransactionEntity entity = TransactionPersistenceMapper.toEntity(transaction);
        JpaTransactionEntity saved = transactionJpaRepository.save(entity);
        return TransactionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return transactionJpaRepository.findById(id.asUuid())
                .map(TransactionPersistenceMapper::toDomain);
    }

    @Override
    public PageResult<Transaction> findByUser(UserId userId, TransactionQuery query, Pagination page) {

        Pageable pageable = toSpringPageable(page);

        var spec = TransactionSpecifications.byUserAndQuery(userId.asUuid(), query);

        var springPage = transactionJpaRepository
                .findAll(spec, pageable)
                .map(TransactionPersistenceMapper::toDomain);

        return new PageResult<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }

    @Override
    public BigDecimal sumAmountByUserAndPeriod(UserId userId, LocalDate fromInclusive, LocalDate toInclusive) {
        return transactionJpaRepository.sumSignedSettledAmountByUserAndPeriod(
                userId.asUuid(), fromInclusive, toInclusive
        );
    }

    @Override
    public PageResult<Transaction> search(UserId userId, TransactionQuery query, Pagination pagination) {
        Pageable pageable = toSpringPageable(pagination);

        Specification<JpaTransactionEntity> spec =
                TransactionSpecifications.byUserAndQuery(userId.asUuid(), query);

        var page = transactionJpaRepository.findAll(spec, pageable);

        List<Transaction> content = page.getContent().stream()
                .map(TransactionPersistenceMapper::toDomain)
                .toList();

        return new PageResult<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Pageable toSpringPageable(Pagination pr) {
        Sort sort = toSpringSortOrDefault(pr.sorts());
        return PageRequest.of(pr.page(), pr.size(), sort);
    }

    private Sort toSpringSortOrDefault(List<Pagination.Sort> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return Sort.by(Sort.Order.desc("createdAt")); // default
        }

        var orders = sorts.stream()
                .filter(s -> s != null && s.field() != null && !s.field().isBlank())
                .map(s -> s.direction() == Pagination.Sort.Direction.ASC
                        ? Sort.Order.asc(s.field())
                        : Sort.Order.desc(s.field()))
                .toList();

        return orders.isEmpty()
                ? Sort.by(Sort.Order.desc("createdAt"))
                : Sort.by(orders);
    }
}
