package com.donyx.lifeops.financeiro.adapters.outbound;

import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;


public class JwtTokenProviderAdapter  implements TokenProvider {

    private final SecretKey key;
    private final Duration accessTtl;

    public JwtTokenProviderAdapter(String secret, Duration accessTtl) {
        // secret precisa ter tamanho decente (>= 32 bytes) pra HS256/HS512 sem dor
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = accessTtl;
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtl);

        return Jwts.builder()
                .subject(user.id().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", user.email())
                .claim("name", user.name())
                .claim("roles", user.roles())
                .signWith(key)
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getSubject(UserId userId) {
        return parse(userId.toString()).getSubject();
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
