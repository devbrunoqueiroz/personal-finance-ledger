package com.donyx.lifeops.financeiro.adapters.outbound;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordHasherAdapterTest {

    @Test
    @DisplayName("hash -> gera hash diferente do raw e matches retorna true")
    void hash_and_matches_ok() {
        BCryptPasswordHasherAdapter hasher = new BCryptPasswordHasherAdapter();

        String raw = "12345678";
        String hash = hasher.hash(raw);

        assertNotNull(hash);
        assertNotEquals(raw, hash);
        assertTrue(hash.startsWith("$2")); // formato bcrypt

        assertTrue(hasher.matches(raw, hash));
    }

    @Test
    @DisplayName("matches -> retorna false quando senha não confere")
    void matches_wrongPassword_false() {
        BCryptPasswordHasherAdapter hasher = new BCryptPasswordHasherAdapter();

        String hash = hasher.hash("12345678");

        assertFalse(hasher.matches("wrong", hash));
    }

    @Test
    @DisplayName("hash -> strength customizado funciona")
    void hash_withCustomStrength_ok() {
        BCryptPasswordHasherAdapter hasher = new BCryptPasswordHasherAdapter(4);
        // strength baixo pra teste rápido

        String raw = "abc123456";
        String hash = hasher.hash(raw);

        assertTrue(hasher.matches(raw, hash));
    }

    @Test
    @DisplayName("matches -> hash inválido retorna false")
    void matches_invalidHash_false() {
        BCryptPasswordHasherAdapter hasher = new BCryptPasswordHasherAdapter();

        assertFalse(hasher.matches("12345678", "hash-aleatorio"));
    }
}