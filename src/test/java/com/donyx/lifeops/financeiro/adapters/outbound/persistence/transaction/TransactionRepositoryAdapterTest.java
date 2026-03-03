package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryAdapterTest {

    @Mock
    TransactionJpaRepository jpaRepository;

    TransactionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TransactionRepositoryAdapter(jpaRepository);
    }

    @Test
    @DisplayName("save -> converte para entity, salva e retorna domínio mapeado")
    void save_mapsAndPersists() {
        Transaction transaction = mock(Transaction.class);
        JpaTransactionEntity entity = new JpaTransactionEntity();
        JpaTransactionEntity savedEntity = new JpaTransactionEntity();
        Transaction mappedBack = mock(Transaction.class);

        try (MockedStatic<TransactionPersistenceMapper> mapper =
                     mockStatic(TransactionPersistenceMapper.class)) {

            mapper.when(() -> TransactionPersistenceMapper.toEntity(transaction))
                    .thenReturn(entity);

            when(jpaRepository.save(entity)).thenReturn(savedEntity);

            mapper.when(() -> TransactionPersistenceMapper.toDomain(savedEntity))
                    .thenReturn(mappedBack);

            Transaction result = adapter.save(transaction);

            assertSame(mappedBack, result);
            verify(jpaRepository).save(entity);
        }
    }

    @Test
    @DisplayName("findById -> retorna Optional com domínio mapeado")
    void findById_mapsCorrectly() {
        UUID uuid = UUID.randomUUID();
        TransactionId id = mock(TransactionId.class);
        when(id.asUuid()).thenReturn(uuid);

        JpaTransactionEntity entity = new JpaTransactionEntity();
        Transaction domain = mock(Transaction.class);

        try (MockedStatic<TransactionPersistenceMapper> mapper =
                     mockStatic(TransactionPersistenceMapper.class)) {

            when(jpaRepository.findById(uuid)).thenReturn(Optional.of(entity));
            mapper.when(() -> TransactionPersistenceMapper.toDomain(entity))
                    .thenReturn(domain);

            Optional<Transaction> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertSame(domain, result.get());
        }
    }

    @Test
    @DisplayName("sumAmountByUserAndPeriod -> delega corretamente ao repository")
    void sumAmount_delegatesCorrectly() {
        UserId userId = mock(UserId.class);
        UUID uuid = UUID.randomUUID();
        when(userId.asUuid()).thenReturn(uuid);

        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();

        BigDecimal expected = BigDecimal.valueOf(1500);

        when(jpaRepository.sumSignedSettledAmountByUserAndPeriod(uuid, from, to))
                .thenReturn(expected);

        BigDecimal result = adapter.sumAmountByUserAndPeriod(userId, from, to);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("search -> aplica sort default (createdAt DESC) quando sorts null")
    void search_appliesDefaultSort() {
        UserId userId = mock(UserId.class);
        UUID uuid = UUID.randomUUID();
        when(userId.asUuid()).thenReturn(uuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(0);
        when(pagination.size()).thenReturn(10);
        when(pagination.sorts()).thenReturn(null);

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec =
                (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic =
                     mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(uuid, query))
                    .thenReturn(spec);

            when(jpaRepository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.search(userId, query, pagination);

            verify(jpaRepository).findAll(eq(spec), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();

            Sort.Order order = pageable.getSort().getOrderFor("createdAt");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }
    }

    @Test
    @DisplayName("search -> aplica sorts customizados corretamente")
    void search_appliesCustomSorts() {
        UserId userId = mock(UserId.class);
        UUID uuid = UUID.randomUUID();
        when(userId.asUuid()).thenReturn(uuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination.Sort sort = mock(Pagination.Sort.class);
        when(sort.field()).thenReturn("amount");
        when(sort.direction()).thenReturn(Pagination.Sort.Direction.ASC);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(1);
        when(pagination.size()).thenReturn(20);
        when(pagination.sorts()).thenReturn(List.of(sort));

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec =
                (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic =
                     mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(uuid, query))
                    .thenReturn(spec);

            when(jpaRepository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.search(userId, query, pagination);

            verify(jpaRepository).findAll(eq(spec), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();

            Sort.Order order = pageable.getSort().getOrderFor("amount");
            assertNotNull(order);
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }
    }

    @Test
    @DisplayName("findByUser -> mapeia Page para PageResult corretamente")
    void findByUser_mapsPageResultCorrectly() {
        UserId userId = mock(UserId.class);
        UUID uuid = UUID.randomUUID();
        when(userId.asUuid()).thenReturn(uuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(0);
        when(pagination.size()).thenReturn(2);
        when(pagination.sorts()).thenReturn(List.of());

        JpaTransactionEntity e1 = new JpaTransactionEntity();
        JpaTransactionEntity e2 = new JpaTransactionEntity();

        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);

        Pageable pageable = PageRequest.of(0, 2);
        Page<JpaTransactionEntity> page =
                new PageImpl<>(List.of(e1, e2), pageable, 5);

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec =
                (Specification<JpaTransactionEntity>) mock(Specification.class);

        try (MockedStatic<TransactionSpecifications> specStatic =
                     mockStatic(TransactionSpecifications.class);
             MockedStatic<TransactionPersistenceMapper> mapperStatic =
                     mockStatic(TransactionPersistenceMapper.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(uuid, query))
                    .thenReturn(spec);

            mapperStatic.when(() -> TransactionPersistenceMapper.toDomain(e1)).thenReturn(t1);
            mapperStatic.when(() -> TransactionPersistenceMapper.toDomain(e2)).thenReturn(t2);

            when(jpaRepository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(page);

            PageResult<Transaction> result =
                    adapter.findByUser(userId, query, pagination);

            assertEquals(2, result.content().size());
            assertEquals(5, result.totalElements());
            assertEquals(3, result.totalPages()); // 5 itens / 2 por página
        }
    }

    @Test
    @DisplayName("search -> quando sorts tem apenas itens inválidos (null/blank), cai no default createdAt DESC")
    void search_whenSortsOnlyInvalid_fallsBackToDefaultSort() {
        UserId userId = mock(UserId.class);
        UUID uuid = UUID.randomUUID();
        when(userId.asUuid()).thenReturn(uuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination.Sort blankField = mock(Pagination.Sort.class);
        when(blankField.field()).thenReturn("   ");

        Pagination.Sort nullField = mock(Pagination.Sort.class);
        when(nullField.field()).thenReturn(null);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(0);
        when(pagination.size()).thenReturn(10);
        // lista NÃO vazia, mas "orders" vai ficar vazio depois do filter
        when(pagination.sorts())
                .thenReturn(new java.util.ArrayList<>(List.of(blankField, nullField)) {{
                    add(0, null);
                }});

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec =
                (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic = mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(uuid, query))
                    .thenReturn(spec);

            when(jpaRepository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.search(userId, query, pagination);

            verify(jpaRepository).findAll(eq(spec), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            Sort.Order order = pageable.getSort().getOrderFor("createdAt");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }
    }

    @Test
    @DisplayName("search -> aplica sort DESC quando direction != ASC")
    void search_appliesDescBranch() {
        UserId userId = mock(UserId.class);
        UUID uuid = UUID.randomUUID();
        when(userId.asUuid()).thenReturn(uuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination.Sort sort = mock(Pagination.Sort.class);
        when(sort.field()).thenReturn("amount");
        when(sort.direction()).thenReturn(Pagination.Sort.Direction.DESC);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(0);
        when(pagination.size()).thenReturn(10);
        when(pagination.sorts()).thenReturn(List.of(sort));

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec =
                (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic = mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(uuid, query))
                    .thenReturn(spec);

            when(jpaRepository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.search(userId, query, pagination);

            verify(jpaRepository).findAll(eq(spec), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            Sort.Order order = pageable.getSort().getOrderFor("amount");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }
    }

    @Test
    @DisplayName("search -> ignora itens inválidos e mantém os válidos (cobrindo filter + map + Sort.by(orders))")
    void search_filtersInvalidAndKeepsValidOrders() {
        UUID uuid = UUID.randomUUID();
        UserId userId = UserId.of(uuid);

        TransactionQuery query = mock(TransactionQuery.class);

        Pagination.Sort invalidBlank = mock(Pagination.Sort.class);
        when(invalidBlank.field()).thenReturn("");

        Pagination.Sort valid1 = mock(Pagination.Sort.class);
        when(valid1.field()).thenReturn("dueDate");
        when(valid1.direction()).thenReturn(Pagination.Sort.Direction.ASC);

        Pagination.Sort valid2 = mock(Pagination.Sort.class);
        when(valid2.field()).thenReturn("amount");
        when(valid2.direction()).thenReturn(Pagination.Sort.Direction.DESC);

        Pagination pagination = mock(Pagination.class);
        when(pagination.page()).thenReturn(0);
        when(pagination.size()).thenReturn(10);
        when(pagination.sorts()).thenReturn(java.util.Arrays.asList(null, invalidBlank, valid1, valid2));

        @SuppressWarnings("unchecked")
        Specification<JpaTransactionEntity> spec =
                (Specification<JpaTransactionEntity>) mock(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        try (MockedStatic<TransactionSpecifications> specStatic = mockStatic(TransactionSpecifications.class)) {

            specStatic.when(() -> TransactionSpecifications.byUserAndQuery(uuid, query))
                    .thenReturn(spec);

            when(jpaRepository.findAll(eq(spec), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adapter.search(userId, query, pagination);

            verify(jpaRepository).findAll(eq(spec), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            // aqui tem que estar ordenado pelos válidos (não cair no default)
            Sort.Order o1 = pageable.getSort().getOrderFor("dueDate");
            Sort.Order o2 = pageable.getSort().getOrderFor("amount");

            assertNotNull(o1);
            assertEquals(Sort.Direction.ASC, o1.getDirection());

            assertNotNull(o2);
            assertEquals(Sort.Direction.DESC, o2.getDirection());

            // e createdAt NÃO deveria ser o sort default aqui
            assertNull(pageable.getSort().getOrderFor("createdAt"));
        }
    }
}