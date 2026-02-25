package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<JpaTransactionEntity, UUID>, JpaSpecificationExecutor<JpaTransactionEntity> {
    @Query("""
        select coalesce(sum(
            case when t.type = com.donyx.lifeops.financeiro.domain.transaction.TransactionType.INCOME
                 then t.amount else -t.amount end
        ), 0)
        from JpaTransactionEntity t
        where t.ownerId = :ownerId
          and t.settledAt is not null
          and t.settledAt >= :fromInclusive
          and t.settledAt <= :toInclusive
    """)
    BigDecimal sumSignedSettledAmountByUserAndPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("fromInclusive") LocalDate fromInclusive,
            @Param("toInclusive") LocalDate toInclusive
    );


}
