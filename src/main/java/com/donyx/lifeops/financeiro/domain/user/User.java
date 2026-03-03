package com.donyx.lifeops.financeiro.domain.user;

import com.donyx.lifeops.financeiro.domain.common.exception.DomainRuleViolationException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public final class User {

    private final UserId id;
    private  String name;
    private final String email;
    private final String passwordHash;
    private  UserId updatedBy;
    private final Instant createdAt;
    private UserStatus status;
    private Set<UserRole> roles;

    public User(UserId id, String name, String email, String passwordHash) {
        this(id, name, email, passwordHash, null, UserStatus.ACTIVE, Collections.singleton(UserRole.USER), Instant.now());
    }

    public User(UserId id, String name, String email, String passwordHash, UserId updatedBy, UserStatus status, Set<UserRole> roles, Instant createdAt) {

        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name").trim();
        this.email = Objects.requireNonNull(email, "email").trim();
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.status = status == null ? UserStatus.ACTIVE    : status;
        this.roles = roles == null ? Collections.singleton(UserRole.USER) : Collections.unmodifiableSet(new HashSet<>(roles));
        if (this.name.isEmpty()) throw new DomainRuleViolationException("Nome não pode ser vazio");
        if (this.email.isEmpty() || !email.contains("@")) throw new DomainRuleViolationException("Email inválido");
        if (this.passwordHash.isEmpty()) throw new DomainRuleViolationException("Senha não pode ser vazia");
    }

    private User(UserId id,
                 String name,
                 String email,
                 String passwordHash,
                 UserId updatedBy,
                 Instant createdAt,
                 UserStatus status,
                 Set<UserRole> roles) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.email = Objects.requireNonNull(email).trim();
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.updatedBy = updatedBy;

        this.createdAt = Objects.requireNonNull(createdAt);

        this.status = status == null ? UserStatus.ACTIVE : status;
        this.roles = roles == null ? Set.of(UserRole.USER) : Set.copyOf(roles);

        if (this.name.isBlank()) throw new DomainRuleViolationException("Nome não pode ser vazio");
        if (!this.email.contains("@")) throw new DomainRuleViolationException("Email inválido");
    }

    public static User rehydrate(
            UserId id,
            String name,
            String email,
            String passwordHash,
            UserId updatedBy,
            Instant createdAt,
            UserStatus status,
            Set<UserRole> roles
    ) {
        return new User(
                id,
                name,
                email,
                passwordHash,
                updatedBy,
                createdAt,
                status,
                roles
        );
    }

    public static User create(String name, String email, String passwordHash) {
        return new User(UserId.random(), name, email, passwordHash);
    }

    public UserId id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public UserId updatedBy() { return updatedBy; }
    public Instant createdAt() { return createdAt; }
    public UserStatus status() { return status; }
    public Set<UserRole> roles() { return roles; }

    public User withPasswordHash(String newHash, UserId updatedBy) {
        return new User(id, name, email, Objects.requireNonNull(newHash), updatedBy, status, roles, createdAt);
    }

    public User activate(UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, UserStatus.ACTIVE, roles, createdAt);
    }

    public User block(UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, UserStatus.BLOCKED, roles, createdAt);
    }

    public User exclude(UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, UserStatus.DELETED, roles, createdAt);
    }

    public User withRoles(Set<UserRole> newRoles, UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, status, newRoles, createdAt);
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name").trim();
        if (this.name.isEmpty()) throw new DomainRuleViolationException("Nome não pode ser vazio");
    }

    public void setUpdatedBy(UserId updatedBy) {
        this.updatedBy =  Objects.requireNonNull(updatedBy);
    }

    public void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles == null ? Collections.singleton(UserRole.USER) : Collections.unmodifiableSet(new HashSet<>(roles));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", roles=" + roles +
                '}';
    }
}
