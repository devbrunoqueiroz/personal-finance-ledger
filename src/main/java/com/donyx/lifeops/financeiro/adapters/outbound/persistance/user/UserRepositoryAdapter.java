package com.donyx.lifeops.financeiro.adapters.outbound.persistance.user;

import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;

import java.util.Optional;

public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataJpaRepository repository;

    public UserRepositoryAdapter(SpringDataJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsActiveByName(name, UserStatus.DELETED);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsActiveByEmail(email, UserStatus.DELETED);
    }

    @Override
    public User save(User user) {
        JpaUserEntity entity = UserPersistanceMapper.toEntity(user);
        JpaUserEntity saved = repository.save(entity);
        return UserPersistanceMapper.toDomain(saved);
    }

    @Override
    public void markDeletedById(UserId id) {
        JpaUserEntity entity = repository.findById(id.asUuid())
                .orElseThrow(() -> new RuntimeException("User not found"));
        entity.setStatus(UserStatus.DELETED);
        repository.save(entity);
    }

    @Override
    public Optional<User> findByName(String name) {
        return repository.findActiveByName(name, UserStatus.DELETED)
                .map(UserPersistanceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }
}
