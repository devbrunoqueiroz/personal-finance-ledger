package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.PageRequest;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class TransactionRepositoryAdapterTest {

    @Test
    void save_mapsEntityAndBack() {
        TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
        TransactionRepositoryAdapter adapter = new TransactionRepositoryAdapter(jpa);

        // Arrange: cria uma entity que será retornada pelo JPA save
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        JpaTransactionEntity savedEntity = new JpaTransactionEntity();
        savedEntity.setId(id);
        savedEntity.setOwnerId(ownerId);
        savedEntity.setDescription("x");
        savedEntity.setAmount(java.math.BigDecimal.ONE);
        savedEntity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        savedEntity.setUpdatedAt(savedEntity.getCreatedAt());
        savedEntity.setType(com.donyx.lifeops.financeiro.domain.transaction.TransactionType.EXPENSE);
        savedEntity.setStatus(com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus.PENDING);
        savedEntity.setDueDate(java.time.LocalDate.of(2026, 1, 1));

        when(jpa.save(any(JpaTransactionEntity.class))).thenReturn(savedEntity);

        // Transaction domínio mínimo: se seu Transaction.hydrate exige tudo, preencha.
        Transaction tx = Transaction.hydrate(
                TransactionId.of(id),
                UserId.of(ownerId),
                "x",
                null,
                java.math.BigDecimal.ONE,
                com.donyx.lifeops.financeiro.domain.transaction.TransactionType.EXPENSE,
                com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus.PENDING,
                java.time.LocalDate.of(2026, 1, 1),
                null,
                null,
                savedEntity.getCreatedAt(),
                savedEntity.getUpdatedAt()
        );

        // Act
        Transaction out = adapter.save(tx);

        // Assert
        assertEquals(id, out.id().asUuid());
        verify(jpa).save(any(JpaTransactionEntity.class));
    }

    @Test
    void findById_delegatesAndMaps() {
        TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
        TransactionRepositoryAdapter adapter = new TransactionRepositoryAdapter(jpa);

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        JpaTransactionEntity entity = new JpaTransactionEntity();
        entity.setId(id);
        entity.setOwnerId(ownerId);
        entity.setDescription("x");
        entity.setAmount(java.math.BigDecimal.ONE);
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setUpdatedAt(entity.getCreatedAt());
        entity.setType(com.donyx.lifeops.financeiro.domain.transaction.TransactionType.EXPENSE);
        entity.setStatus(com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus.PENDING);
        entity.setDueDate(java.time.LocalDate.of(2026, 1, 1));

        when(jpa.findById(id)).thenReturn(Optional.of(entity));

        Optional<Transaction> found = adapter.findById(TransactionId.of(id));

        assertTrue(found.isPresent());
        assertEquals(id, found.get().id().asUuid());
        verify(jpa).findById(id);
    }

    @Test
    void findByUser_buildsPageableAndMapsPageResult() {
        TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
        TransactionRepositoryAdapter adapter = new TransactionRepositoryAdapter(jpa);

        UUID userUuid = UUID.randomUUID();
        UserId userId = UserId.of(userUuid);

        TransactionQuery query = mock(TransactionQuery.class);

        PageRequest page = new PageRequest(
                1,
                20,
                new PageRequest.Sort("ignored", PageRequest.Sort.Direction.ASC)
        );

        JpaTransactionEntity e = new JpaTransactionEntity();
        e.setId(UUID.randomUUID());
        e.setOwnerId(userUuid);
        e.setDescription("x");
        e.setNotes(null);
        e.setAmount(java.math.BigDecimal.ONE);
        e.setType(TransactionType.EXPENSE);
        e.setStatus(TransactionStatus.PENDING);
        e.setDueDate(LocalDate.of(2026, 1, 1));
        e.setSettledAt(null);
        e.setCategoryId(null);
        e.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        e.setUpdatedAt(e.getCreatedAt());

        Page<JpaTransactionEntity> springPage = new PageImpl<>(
                List.of(e),
                org.springframework.data.domain.PageRequest.of(1, 20),
                55
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(jpa.findAll(
                ArgumentMatchers.<Specification<JpaTransactionEntity>>any(),
                pageableCaptor.capture()
        )).thenReturn(springPage);

        PageResult<Transaction> result = adapter.findByUser(userId, query, page);

        // ---- PageResult conforme seu record
        assertEquals(1, result.items().size());
        assertEquals(55, result.totalItems());
        assertEquals(1, result.page());
        assertEquals(20, result.size());

        // ---- Pageable criado pelo adapter
        Pageable pageable = pageableCaptor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void sumAmountByUserAndPeriod_currentlyNotImplemented() {
        TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
        TransactionRepositoryAdapter adapter = new TransactionRepositoryAdapter(jpa);

        assertNull(adapter.sumAmountByUserAndPeriod(
                UserId.of(UUID.randomUUID()),
                java.time.LocalDate.now(),
                java.time.LocalDate.now()
        ));
    }
}