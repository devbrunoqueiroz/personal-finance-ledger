package com.donyx.lifeops.financeiro.domain.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    @Test
    void shouldCreateValidTransaction() {
        Transaction t = new Transaction(TransactionId.random(), UserId.random(), BigDecimal.valueOf(100), TransactionType.INCOME, Instant.now());
        assertEquals(BigDecimal.valueOf(100), t.amount());
        assertEquals(TransactionType.INCOME, t.type());
    }

    @Test
    void shouldThrowOnNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(TransactionId.random(), UserId.random(), BigDecimal.valueOf(-10), TransactionType.EXPENSE, Instant.now())
        );
    }

    @Test
    void shouldSetCategoryId() {
        Transaction t = new Transaction(TransactionId.random(), UserId.random(), BigDecimal.valueOf(50), TransactionType.EXPENSE, Instant.now());
        CategoryId catId = CategoryId.random();
        t.setCategoryId(catId);
        assertEquals(catId, t.categoryId());
    }
}
