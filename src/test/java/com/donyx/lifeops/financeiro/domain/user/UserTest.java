package com.donyx.lifeops.financeiro.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private UserId userId;
    private Set<UserRole> roles;

    @BeforeEach
    void setUp(){
        this.userId = UserId.random();
        this.roles = Collections.singleton(UserRole.USER);
    }
    @Test
    void shouldCreateValidUser() {
        User user = new User(
            UserId.random(),
            "Test User",
            "test@email.com",
            "hash",
            null,
            UserStatus.ACTIVE,
            Collections.singleton(UserRole.USER),
            Instant.now()
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
                userId,
                "Test User",
                "invalid",
                "hash",
                null,
                UserStatus.ACTIVE,
                roles, Instant.now()
            )
        );
    }

    @Test
    void shouldThrowOnNullName() {
        assertThrows(NullPointerException.class, () ->
            new User(
                userId,
                null,
                "test@email.com",
                "hash",
                null,
                UserStatus.ACTIVE,
                roles, Instant.now()
            )
        );
    }
}
