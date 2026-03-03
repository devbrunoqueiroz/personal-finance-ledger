package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionSpecificationsTest {

    @Autowired
    TransactionJpaRepository repository;


    private JpaTransactionEntity tx(UUID ownerId) {
        JpaTransactionEntity e = new JpaTransactionEntity();
        e.setId(UUID.randomUUID());
        e.setOwnerId(ownerId);

        e.setDescription("desc");
        e.setNotes("notes");
        e.setAmount(BigDecimal.ZERO);
        e.setDueDate(LocalDate.of(2026, 1, 1));
        e.setSettledAt(LocalDate.of(2026, 1, 1));
        e.setType(TransactionType.EXPENSE);
        e.setStatus(TransactionStatus.PENDING);

        e.setCategoryId(UUID.randomUUID());
        Instant now = Instant.parse("2026-02-26T00:00:00Z");
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return e;
    }

    private TransactionQuery q() {
        TransactionQuery q = mock(TransactionQuery.class);
        // por padrão, tudo null
        when(q.text()).thenReturn(null);
        when(q.minAmount()).thenReturn(null);
        when(q.maxAmount()).thenReturn(null);
        when(q.dueFrom()).thenReturn(null);
        when(q.dueTo()).thenReturn(null);
        when(q.settledFrom()).thenReturn(null);
        when(q.settledTo()).thenReturn(null);
        when(q.type()).thenReturn(null);
        when(q.status()).thenReturn(null);
        when(q.categoryId()).thenReturn(null);
        return q;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("byUserAndQuery -> quando q é null, filtra apenas por ownerId")
    void byUserAndQuery_whenQueryNull_filtersOnlyByOwner() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        repository.saveAll(List.of(
                tx(u1),
                tx(u1),
                tx(u2)
        ));

        var spec = TransactionSpecifications.byUserAndQuery(u1, null);

        List<JpaTransactionEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getOwnerId().equals(u1)));
    }

    @Test
    @DisplayName("byUserAndQuery -> text: trim + case-insensitive em description OU notes")
    void byUserAndQuery_text_matchesDescriptionOrNotes_caseInsensitive() {
        UUID u = UUID.randomUUID();

        JpaTransactionEntity a = tx(u);
        a.setDescription("Compra no Mercado");
        a.setNotes("nada");

        JpaTransactionEntity b = tx(u);
        b.setDescription("nada");
        b.setNotes("PAGAMENTO do aluguel");

        JpaTransactionEntity c = tx(u);
        c.setDescription("nada");
        c.setNotes("nada");

        repository.saveAll(List.of(a, b, c));

        TransactionQuery q = q();
        when(q.text()).thenReturn("   paGAMENto  "); // deve bater em notes do b (lower + like)

        var spec = TransactionSpecifications.byUserAndQuery(u, q);

        List<JpaTransactionEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals(b.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("byUserAndQuery -> minAmount/maxAmount são inclusivos")
    void byUserAndQuery_amountRange_inclusive() {
        UUID u = UUID.randomUUID();

        JpaTransactionEntity a = tx(u); a.setAmount(new BigDecimal("10.00"));
        JpaTransactionEntity b = tx(u); b.setAmount(new BigDecimal("20.00"));
        JpaTransactionEntity c = tx(u); c.setAmount(new BigDecimal("30.00"));

        repository.saveAll(List.of(a, b, c));

        TransactionQuery q = q();
        when(q.minAmount()).thenReturn(new BigDecimal("20.00"));
        when(q.maxAmount()).thenReturn(new BigDecimal("30.00"));

        var spec = TransactionSpecifications.byUserAndQuery(u, q);

        List<JpaTransactionEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(b.getId())));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(c.getId())));
    }

    @Test
    @DisplayName("byUserAndQuery -> dueFrom/dueTo são inclusivos")
    void byUserAndQuery_dueDateRange_inclusive() {
        UUID u = UUID.randomUUID();

        JpaTransactionEntity a = tx(u); a.setDueDate(LocalDate.of(2026, 1, 1));
        JpaTransactionEntity b = tx(u); b.setDueDate(LocalDate.of(2026, 1, 10));
        JpaTransactionEntity c = tx(u); c.setDueDate(LocalDate.of(2026, 1, 20));

        repository.saveAll(List.of(a, b, c));

        TransactionQuery q = q();
        when(q.dueFrom()).thenReturn(LocalDate.of(2026, 1, 10));
        when(q.dueTo()).thenReturn(LocalDate.of(2026, 1, 20));

        var spec = TransactionSpecifications.byUserAndQuery(u, q);

        List<JpaTransactionEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(b.getId())));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(c.getId())));
    }

    @Test
    @DisplayName("byUserAndQuery -> settledFrom/settledTo são inclusivos")
    void byUserAndQuery_settledAtRange_inclusive() {
        UUID u = UUID.randomUUID();

        JpaTransactionEntity a = tx(u); a.setSettledAt(LocalDate.of(2025, 12, 31));
        JpaTransactionEntity b = tx(u); b.setSettledAt(LocalDate.of(2026, 1, 1));
        JpaTransactionEntity c = tx(u); c.setSettledAt(LocalDate.of(2026, 1, 1));

        repository.saveAll(List.of(a, b, c));

        TransactionQuery q = q();
        when(q.settledFrom()).thenReturn(LocalDate.of(2026, 1, 1));
        when(q.settledTo()).thenReturn(LocalDate.of(2026, 1, 1));

        var spec = TransactionSpecifications.byUserAndQuery(u, q);

        List<JpaTransactionEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(b.getId())));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(c.getId())));
    }

    @Test
    @DisplayName("byUserAndQuery -> filtra por type, status e categoryId.asUuid()")
    void byUserAndQuery_filtersByTypeStatusAndCategory() {
        UUID u = UUID.randomUUID();

        UUID cat1 = UUID.randomUUID();
        UUID cat2 = UUID.randomUUID();

        JpaTransactionEntity ok = tx(u);
        ok.setType(TransactionType.INCOME);
        ok.setStatus(TransactionStatus.COMPLETED);
        ok.setCategoryId(cat1);

        JpaTransactionEntity wrongType = tx(u);
        wrongType.setType(TransactionType.EXPENSE);
        wrongType.setStatus(TransactionStatus.COMPLETED);
        wrongType.setCategoryId(cat1);

        JpaTransactionEntity wrongStatus = tx(u);
        wrongStatus.setType(TransactionType.INCOME);
        wrongStatus.setStatus(TransactionStatus.PENDING);
        wrongStatus.setCategoryId(cat1);

        JpaTransactionEntity wrongCategory = tx(u);
        wrongCategory.setType(TransactionType.INCOME);
        wrongCategory.setStatus(TransactionStatus.COMPLETED);
        wrongCategory.setCategoryId(cat2);

        repository.saveAll(List.of(ok, wrongType, wrongStatus, wrongCategory));

        CategoryId categoryId = mock(CategoryId.class);
        when(categoryId.asUuid()).thenReturn(cat1);

        TransactionQuery q = q();
        when(q.type()).thenReturn(TransactionType.INCOME);
        when(q.status()).thenReturn(TransactionStatus.COMPLETED);
        when(q.categoryId()).thenReturn(categoryId);

        var spec = TransactionSpecifications.byUserAndQuery(u, q);

        List<JpaTransactionEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals(ok.getId(), result.get(0).getId());
    }
}