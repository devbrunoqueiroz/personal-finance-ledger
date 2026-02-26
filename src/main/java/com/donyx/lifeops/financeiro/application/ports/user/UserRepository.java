package com.donyx.lifeops.financeiro.application.ports.user;

import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.util.Optional;

public interface UserRepository {

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    User save(User user);

    void markDeletedById(UserId id);

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UserId id);
}
