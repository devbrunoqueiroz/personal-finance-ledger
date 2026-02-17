package com.donyx.lifeops.financeiro.adapters.outbound.persistance;

import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.time.Instant;

public class UserPersistanceMapper {
    public static JpaUserEntity toEntity(User user) {
        JpaUserEntity e = new JpaUserEntity();
        e.setId(user.id().asUuid());
        e.setEmail(user.email());
        e.setPasswordHash(user.passwordHash());
        e.setRoles(user.roles());
        e.setStatus(user.status());
        return e;
    }

    public static User toDomain(JpaUserEntity e) {
        return new User(
            UserId.of(e.getId()),
                e.getName(),
                e.getEmail(),
                e.getPasswordHash(),
                null,
                Instant.now(),
                Instant.now(),
                e.getStatus(),
                e.getRoles());

    }
}
