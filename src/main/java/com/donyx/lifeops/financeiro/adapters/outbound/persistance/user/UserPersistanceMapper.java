package com.donyx.lifeops.financeiro.adapters.outbound.persistance.user;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public final class UserPersistanceMapper {

    private UserPersistanceMapper() {
    }

    public static JpaUserEntity toEntity(User user) {
        JpaUserEntity e = new JpaUserEntity();

        e.setId(user.id().asUuid());
        e.setName(user.name());
        e.setEmail(user.email());
        e.setPasswordHash(user.passwordHash());

        e.setStatus(user.status());
        e.setRoles(user.roles());

        e.setCreatedAt(user.createdAt());
        e.setUpdatedAt(user.updatedAt());

        e.setUpdatedBy(
                user.updatedBy() != null
                        ? user.updatedBy().asUuid()
                        : null
        );

        return e;
    }

    public static User toDomain(JpaUserEntity e) {
        return User.rehydrate(
                UserId.of(e.getId()),
                e.getName(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getUpdatedBy() != null
                        ? UserId.of(e.getUpdatedBy())
                        : null,
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getStatus(),
                e.getRoles()
        );
    }
}