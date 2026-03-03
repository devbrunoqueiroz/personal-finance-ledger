package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionQueryAdapterTest {

    @Mock
    TransactionJpaRepository repository;

    TransactionQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TransactionQueryAdapter(repository);
    }

    @Test
    @DisplayName("find -> usa ownerId.asUuid, monta spec e chama repository.findAll(spec, pageable)")
    void find_callsRepositoryWithSpecAndPageable() {
        UUID ownerUuid = UUID.randomUUID();
        UserId ownerId = mock(UserId.class);
        when(ownerId.asUuid()).thenReturn(ownerUuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(0);
        when(pagination.size()).thenReturn(10);
        when(pagination.sorts()).thenReturn(null); // cobre caso null

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec = (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic = mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(ownerUuid, query))
                    .thenReturn(spec);

            when(repository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.find(ownerId, query, pagination);

            specStatic.verify(() -> TransactionSpecifications.byUserAndQuery(ownerUuid, query), times(1));
            verify(repository, times(1)).findAll(eq(spec), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(0, pageable.getPageNumber());
            assertEquals(10, pageable.getPageSize());
            assertTrue(pageable.getSort().isUnsorted());
        }
    }

    @Test
    @DisplayName("find -> converte Pagination.sorts em Sort ASC/DESC corretamente")
    void find_buildsPageableWithSortOrders() {
        UUID ownerUuid = UUID.randomUUID();
        UserId ownerId = mock(UserId.class);
        when(ownerId.asUuid()).thenReturn(ownerUuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination.Sort sort1 = mock(Pagination.Sort.class);
        when(sort1.field()).thenReturn("dueDate");
        when(sort1.direction()).thenReturn(Pagination.Sort.Direction.ASC);

        Pagination.Sort sort2 = mock(Pagination.Sort.class);
        when(sort2.field()).thenReturn("amount");
        when(sort2.direction()).thenReturn(Pagination.Sort.Direction.DESC);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(2);
        when(pagination.size()).thenReturn(25);
        when(pagination.sorts()).thenReturn(List.of(sort1, sort2));

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec = (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic = mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(ownerUuid, query))
                    .thenReturn(spec);

            when(repository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.find(ownerId, query, pagination);

            verify(repository).findAll(eq(spec), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertEquals(2, pageable.getPageNumber());
            assertEquals(25, pageable.getPageSize());

            List<Sort.Order> orders = pageable.getSort().toList();
            assertEquals(2, orders.size());

            assertEquals("dueDate", orders.get(0).getProperty());
            assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());

            assertEquals("amount", orders.get(1).getProperty());
            assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
        }
    }

    @Test
    @DisplayName("find -> mapeia entidades para domínio e popula PageResult com metadados do Page")
    void find_mapsEntitiesAndReturnsPageResult() {
        UUID ownerUuid = UUID.randomUUID();
        UserId ownerId = mock(UserId.class);
        when(ownerId.asUuid()).thenReturn(ownerUuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(1);
        when(pagination.size()).thenReturn(2);
        when(pagination.sorts()).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec = (Specification<JpaTransactionEntity>) mock(Specification.class);

        JpaTransactionEntity e1 = new JpaTransactionEntity();
        JpaTransactionEntity e2 = new JpaTransactionEntity();

        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);

        Pageable expectedPageable = PageRequest.of(1, 2, Sort.unsorted());
        Page<JpaTransactionEntity> page = new PageImpl<>(
                List.of(e1, e2),
                expectedPageable,
                7 // totalElements
        );

        try (MockedStatic<TransactionSpecifications> specStatic = mockStatic(TransactionSpecifications.class);
             MockedStatic<TransactionPersistenceMapper> mapperStatic = mockStatic(TransactionPersistenceMapper.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(ownerUuid, query))
                    .thenReturn(spec);

            mapperStatic.when(() -> TransactionPersistenceMapper.toDomain(e1)).thenReturn(t1);
            mapperStatic.when(() -> TransactionPersistenceMapper.toDomain(e2)).thenReturn(t2);

            when(repository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(page);

            PageResult<Transaction> result = adapter.find(ownerId, query, pagination);

            // Conteúdo mapeado
            // (se teu PageResult NÃO for record com content(), ajusta pra getContent() ou fields)
            assertEquals(2, result.content().size());
            assertSame(t1, result.content().get(0));
            assertSame(t2, result.content().get(1));

            // Metadados
            assertEquals(1, result.page());
            assertEquals(2, result.size());
            assertEquals(7, result.totalElements());
            assertEquals(4, result.totalPages()); // 7 itens / 2 por página => 4 páginas

            mapperStatic.verify(() -> TransactionPersistenceMapper.toDomain(e1), times(1));
            mapperStatic.verify(() -> TransactionPersistenceMapper.toDomain(e2), times(1));
        }
    }
}