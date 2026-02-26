package com.donyx.lifeops.financeiro.domain.category;

import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {
    private CategoryId categoryId;
    private UserId userId;
    private Instant now;

    @Test
    void shouldCreateValidCategory() {
        Category c = new Category(CategoryId.random(), UserId.random(), "Salary", "Monthly salary", CategoryType.INCOME, Instant.now(), Instant.now());
        assertEquals("Salary", c.name());
        assertEquals(CategoryType.INCOME, c.type());
    }

    @BeforeEach
    void setUp(){
        this.categoryId = CategoryId.random();
        this.userId = UserId.random();
        this.now = Instant.now();
    }

    @Test
    void shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(categoryId, userId, "   ", "desc", CategoryType.EXPENSE, now, now)
        );
    }

    @Test
    void shouldSetDescription() {
        Category c = new Category(categoryId, userId, "Food", null, CategoryType.EXPENSE, now, now);
        c.setDescription("Groceries");
        assertEquals("Groceries", c.description());
    }
}
