package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.PageRequest;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public PageResult<Transaction> findByUser(UserId userId, TransactionQuery query, PageRequest page) {

        var pageable = org.springframework.data.domain.PageRequest.of(
                page.page(),
                page.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var spec = TransactionSpecifications.byUserAndQuery(userId.asUuid(), query);

        Page<JpaTransactionEntity> result = transactionJpaRepository.findAll(spec, pageable);

        var items = result.getContent().stream()
                .map(TransactionPersistenceMapper::toDomain)
                .toList();

        return new PageResult<>(
                items,
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Override
    public BigDecimal sumAmountByUserAndPeriod(UserId userId, LocalDate fromInclusive, LocalDate toInclusive) {
        return null;
    }
}
