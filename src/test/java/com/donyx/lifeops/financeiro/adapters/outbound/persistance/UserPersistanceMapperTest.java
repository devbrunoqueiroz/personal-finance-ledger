package com.donyx.lifeops.financeiro.adapters.outbound.persistance;

import com.donyx.lifeops.financeiro.adapters.outbound.persistance.user.JpaUserEntity;
import com.donyx.lifeops.financeiro.adapters.outbound.persistance.user.UserPersistanceMapper;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserRole;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPersistanceMapperTest {

    @Test
    void toEntity_mapsAllFields() {
        var user = new User(
                UserId.of(UUID.randomUUID()),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER)
        );

        JpaUserEntity entity = UserPersistanceMapper.toEntity(user);

        assertEquals(user.id().asUuid(), entity.getId());
        assertEquals(user.email(), entity.getEmail());
        assertEquals(user.passwordHash(), entity.getPasswordHash());
        assertEquals(user.roles(), entity.getRoles());
        assertEquals(user.status(), entity.getStatus());
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();

        JpaUserEntity e = new JpaUserEntity();
        e.setId(id);
        e.setName("Bruno");
        e.setEmail("a@b.com");
        e.setPasswordHash("HASH");
        e.setStatus(UserStatus.ACTIVE);
        e.setRoles(Set.of(UserRole.USER));

        User user = UserPersistanceMapper.toDomain(e);

        assertEquals(id, user.id().asUuid());
        assertEquals("Bruno", user.name());
        assertEquals("a@b.com", user.email());
        assertEquals("HASH", user.passwordHash());
        assertEquals(UserStatus.ACTIVE, user.status());
        assertEquals(Set.of(UserRole.USER), user.roles());
    }

    @Test
    void toEntity_nullUser_throws() {
        assertThrows(NullPointerException.class,
                () -> UserPersistanceMapper.toEntity(null));
    }
}