package com.donyx.lifeops.financeiro.adapters.outbound;

import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserRole;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderAdapterTest {

    private static final String SECRET =
            "12345678901234567890123456789012"; // 32 bytes mínimo

    @Test
    @DisplayName("generateAccessToken -> token válido com subject e claims")
    void generateToken_ok() {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, Duration.ofHours(2));

        UserId id = UserId.of(UUID.randomUUID());

        User user = new User(
                id,
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER)
        );

        String token = provider.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(provider.isValid(token));
    }

    @Test
    @DisplayName("isValid -> false quando token inválido")
    void isValid_invalidToken_false() {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, Duration.ofHours(1));

        assertFalse(provider.isValid("token-lixo"));
    }

    @Test
    @DisplayName("token expira conforme TTL")
    void token_expiration_respected() throws InterruptedException {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, Duration.ofMillis(800));

        User user = new User(
                UserId.of(UUID.randomUUID()),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER)
        );

        String token = provider.generateAccessToken(user);

        // tem que ser válido logo após criar
        assertTrue(provider.isValid(token));

        // espera passar o TTL com margem
        Thread.sleep(1000);

        assertFalse(provider.isValid(token));
    }

    @Test
    @DisplayName("token inválido com secret diferente")
    void token_invalid_withDifferentSecret() {
        JwtTokenProviderAdapter provider1 =
                new JwtTokenProviderAdapter(SECRET, Duration.ofHours(1));

        JwtTokenProviderAdapter provider2 =
                new JwtTokenProviderAdapter(
                        "outra-chave-super-secreta-32bytes!!!!",
                        Duration.ofHours(1));

        User user = new User(
                UserId.of(UUID.randomUUID()),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER)
        );

        String token = provider1.generateAccessToken(user);

        assertFalse(provider2.isValid(token));
    }

    @Test
    @DisplayName("subject é userId")
    void subject_isUserId() {
        JwtTokenProviderAdapter provider =
                new JwtTokenProviderAdapter(SECRET, Duration.ofHours(1));

        UserId id = UserId.of(UUID.randomUUID());

        User user = new User(
                id,
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER)
        );

        String token = provider.generateAccessToken(user);

        // parse direto
        String subject = provider.getSubject(token);
        assertEquals(id.toString(), subject);

        assertNotNull(subject);
    }
}