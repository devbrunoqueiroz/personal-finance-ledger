package com.donyx.lifeops.financeiro.application.ports.user;

public interface PasswordHasher {
    String hash(String raw);
    boolean matches(String raw, String hash);
}
