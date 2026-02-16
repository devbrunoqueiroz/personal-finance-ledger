package com.donyx.lifeops.financeiro.domain.category;

import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {
    @Test
    void shouldCreateValidCategory() {
        Category c = new Category(CategoryId.random(), UserId.random(), "Salary", "Monthly salary", CategoryType.INCOME, Instant.now());
        assertEquals("Salary", c.name());
        assertEquals(CategoryType.INCOME, c.type());
    }

    @Test
    void shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(CategoryId.random(), UserId.random(), "   ", "desc", CategoryType.EXPENSE, Instant.now())
        );
    }

    @Test
    void shouldSetDescription() {
        Category c = new Category(CategoryId.random(), UserId.random(), "Food", null, CategoryType.EXPENSE, Instant.now());
        c.setDescription("Groceries");
        assertEquals("Groceries", c.description());
    }
}
