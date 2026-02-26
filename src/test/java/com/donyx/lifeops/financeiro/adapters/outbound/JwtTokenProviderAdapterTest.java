package com.donyx.lifeops.financeiro.adapters.outbound;

import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserRole;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderAdapterTest {

    private static final String SECRET =
            "2biBnmC+Rdj1DxrMa8C7AI9Sesih67cbtNSA4I4UpwY="; // 32 bytes mínimo

    @Test
    @DisplayName("generateAccessToken -> token válido com subject e claims")
    void generateToken_ok() {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, 3000);

        UserId id = UserId.of(UUID.randomUUID());

        User user = new User(
                id,
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER),
                Instant.now()
        );

        String token = provider.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(provider.isValid(token));
    }

    @Test
    @DisplayName("isValid -> false quando token inválido")
    void isValid_invalidToken_false() {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, 3000);

        assertFalse(provider.isValid("token-lixo"));
    }

    @Test
    @DisplayName("token expira conforme TTL")
    void token_expiration_respected() throws InterruptedException {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, 1);

        User user = new User(
                UserId.of(UUID.randomUUID()),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER),
                Instant.now()
        );

        String token = provider.generateAccessToken(user);

        // tem que ser válido logo após criar
        assertTrue(provider.isValid(token));

        // espera passar o TTL com margem
        Thread.sleep(2000);

        assertFalse(provider.isValid(token));
    }

    @Test
    @DisplayName("token inválido com secret diferente")
    void token_invalid_withDifferentSecret() {
        JwtTokenProviderAdapter provider1 =
                new JwtTokenProviderAdapter(SECRET, 3600);

        JwtTokenProviderAdapter provider2 =
                new JwtTokenProviderAdapter(
                        "5CxQFJ5J09onmL+h6kZqfkJ6euTtM+WpsrmAhxaXu3o=",
                        3600);

        User user = new User(
                UserId.of(UUID.randomUUID()),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER),
                Instant.now()
        );

        String token = provider1.generateAccessToken(user);

        assertFalse(provider2.isValid(token));
    }

    @Test
    @DisplayName("subject é id do usuário")
    void subject_isUserId() {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, 3600);

        UserId id = UserId.of(UUID.randomUUID());
        String email = "a@b.com";

        User user = new User(
                id,
                "Bruno",
                email,
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER),
                Instant.now()
        );

        String token = provider.generateAccessToken(user);

        // parse direto
        String subject = provider.getSubject(token);
        assertEquals(id.toString(), subject);

        assertNotNull(subject);
    }
}