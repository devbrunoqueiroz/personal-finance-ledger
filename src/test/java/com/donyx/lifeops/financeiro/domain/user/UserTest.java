package com.donyx.lifeops.financeiro.domain.user;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void shouldCreateValidUser() {
        User user = new User(
            UserId.random(),
            "Test User",
            "test@email.com",
            "hash",
            null,
            Instant.now(),
            Instant.now(),
            UserStatus.ACTIVE,
            Collections.singleton(UserRole.USER)
        );
        assertEquals("Test User", user.name());
        assertEquals("test@email.com", user.email());
        assertEquals(UserStatus.ACTIVE, user.status());
        assertTrue(user.roles().contains(UserRole.USER));
    }

    @Test
    void shouldThrowOnInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            new User(
                UserId.random(),
                "Test User",
                "invalid",
                "hash",
                null,
                Instant.now(),
                Instant.now(),
                UserStatus.ACTIVE,
                Collections.singleton(UserRole.USER)
            )
        );
    }

    @Test
    void shouldThrowOnNullName() {
        assertThrows(NullPointerException.class, () ->
            new User(
                UserId.random(),
                null,
                "test@email.com",
                "hash",
                null,
                Instant.now(),
                Instant.now(),
                UserStatus.ACTIVE,
                Collections.singleton(UserRole.USER)
            )
        );
    }
}
