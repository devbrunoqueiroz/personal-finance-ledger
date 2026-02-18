package com.donyx.lifeops.financeiro.domain.user;

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
    private Instant updatedAt;
    private UserStatus status;
    private Set<UserRole> roles;

    public User(UserId id, String name, String email, String passwordHash) {
        this(id, name, email, passwordHash, null, UserStatus.ACTIVE, Collections.singleton(UserRole.USER));
    }

    public User(UserId id, String name, String email, String passwordHash, UserId updatedBy, UserStatus status, Set<UserRole> roles) {
        var now = Instant.now();

        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name").trim();
        this.email = Objects.requireNonNull(email, "email").trim();
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.updatedBy = updatedBy;
        this.createdAt = now;
        this.updatedAt = now;
        this.status = status == null ? UserStatus.ACTIVE    : status;
        this.roles = roles == null ? Collections.singleton(UserRole.USER) : Collections.unmodifiableSet(new HashSet<>(roles));
        if (this.name.isEmpty()) throw new IllegalArgumentException("Nome não pode ser vazio");
        if (this.email.isEmpty() || !email.contains("@")) throw new IllegalArgumentException("Email inválido");
        if (this.passwordHash.isEmpty()) throw new IllegalArgumentException("Senha não pode ser vazia");
    }

    public static User create(String name, String email, String passwordHash, Instant now) {
        return new User(UserId.random(), name, email, passwordHash, now, now);
    }

    public UserId id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public UserId updatedBy() { return updatedBy; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public UserStatus status() { return status; }
    public Set<UserRole> roles() { return roles; }

    public User withPasswordHash(String newHash, UserId updatedBy) {
        return new User(id, name, email, Objects.requireNonNull(newHash), updatedBy, status, roles);
    }

    public User activate(UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, UserStatus.ACTIVE, roles);
    }

    public User block(UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, UserStatus.BLOCKED, roles);
    }

    public User exclude(UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, UserStatus.DELETED, roles);
    }

    public User withRoles(Set<UserRole> newRoles, UserId updatedBy) {
        return new User(id, name, email, passwordHash, updatedBy, status, newRoles);
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name").trim();
        if (this.name.isEmpty()) throw new IllegalArgumentException("Nome não pode ser vazio");
        this.updatedAt = Instant.now();
    }

    public void setUpdatedBy(UserId updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "status");
        this.updatedAt = Instant.now();
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles == null ? Collections.singleton(UserRole.USER) : Collections.unmodifiableSet(new HashSet<>(roles));
        this.updatedAt = Instant.now();
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
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
