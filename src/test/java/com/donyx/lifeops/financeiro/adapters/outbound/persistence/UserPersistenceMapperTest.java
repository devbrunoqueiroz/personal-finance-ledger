package com.donyx.lifeops.financeiro.adapters.outbound.persistence;

import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.JpaUserEntity;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.UserPersistenceMapper;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserRole;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPersistenceMapperTest {

    @Test
    void toEntity_mapsAllFields() {
        var user = new User(
                UserId.of(UUID.randomUUID()),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER),
                Instant.now()
        );

        JpaUserEntity entity = UserPersistenceMapper.toEntity(user);

        assertEquals(user.id().asUuid(), entity.getId());
        assertEquals(user.email(), entity.getEmail());
        assertEquals(user.passwordHash(), entity.getPasswordHash());
        assertEquals(user.roles(), entity.getRoles());
        assertEquals(user.status(), entity.getStatus());
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-02-21T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-02-21T01:00:00Z");

        JpaUserEntity e = new JpaUserEntity();
        e.setId(id);
        e.setName("Bruno");
        e.setEmail("a@b.com");
        e.setPasswordHash("HASH");
        e.setStatus(UserStatus.ACTIVE);
        e.setRoles(Set.of(UserRole.USER));
        e.setCreatedAt(createdAt);
        e.setUpdatedAt(updatedAt);
        e.setUpdatedBy(updatedBy);

        User user = UserPersistenceMapper.toDomain(e);

        assertEquals(id, user.id().asUuid());
        assertEquals("Bruno", user.name());
        assertEquals("a@b.com", user.email());
        assertEquals("HASH", user.passwordHash());
        assertEquals(UserStatus.ACTIVE, user.status());
        assertEquals(Set.of(UserRole.USER), user.roles());
        assertEquals(createdAt, user.createdAt());
        assertEquals(updatedBy, user.updatedBy().asUuid());
    }

    @Test
    void toEntity_nullUser_throws() {
        assertThrows(NullPointerException.class,
                () -> UserPersistenceMapper.toEntity(null));
    }
}