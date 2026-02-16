package com.donyx.lifeops.financeiro.domain.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {

    @Test
    void random_shouldProduceNonNullUuid() {
        UserId id = UserId.random();
        assertNotNull(id);
        assertNotNull(id.asUuid());
    }

    @Test
    void ofUuid_shouldWrapUuid() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.of(uuid);
        assertEquals(uuid, id.asUuid());
        assertEquals(id, UserId.of(uuid));
    }

    @Test
    void ofString_shouldParseValidUuid() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.of(uuid.toString());
        assertEquals(uuid, id.asUuid());
    }

    @Test
    void ofString_invalid_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> UserId.of("not-a-uuid"));
    }
}
