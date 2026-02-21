package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<JpaTransactionEntity, UUID>, JpaSpecificationExecutor<JpaTransactionEntity> {
}
