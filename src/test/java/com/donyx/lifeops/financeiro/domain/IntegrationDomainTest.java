package com.donyx.lifeops.financeiro.domain;

import com.donyx.lifeops.financeiro.domain.user.*;
import com.donyx.lifeops.financeiro.domain.category.*;
import com.donyx.lifeops.financeiro.domain.transaction.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationDomainTest {
    @Test
    void shouldLinkTransactionToUserAndCategory() {
        User user = new User(UserId.random(), "Ana", "ana@email.com", "hash", null, UserStatus.ACTIVE, java.util.Collections.singleton(UserRole.USER));
        Category category = new Category(CategoryId.random(), user.id(), "Food", "Groceries", CategoryType.EXPENSE, Instant.now(), Instant.now());
        Transaction transaction = new Transaction(TransactionId.random(), user.id(), BigDecimal.valueOf(120), TransactionType.EXPENSE, Instant.now());
        transaction.setCategoryId(category.id());
        assertEquals(user.id(), transaction.ownerId());
        assertEquals(category.id(), transaction.categoryId());
    }

    @Test
    void shouldUpdateTransactionFields() {
        Transaction transaction = new Transaction(TransactionId.random(), UserId.random(), BigDecimal.valueOf(200), TransactionType.INCOME, Instant.now());
        transaction.setDescription("Salary");
        transaction.setNotes("February");
        assertEquals("Salary", transaction.description());
        assertEquals("February", transaction.notes());
    }
}
