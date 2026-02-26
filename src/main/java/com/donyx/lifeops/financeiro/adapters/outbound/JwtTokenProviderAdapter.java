package com.donyx.lifeops.financeiro.adapters.outbound;

import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.domain.user.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenProviderAdapter  implements TokenProvider {

    private final Duration ttl;
    private final SecretKey key;

    public JwtTokenProviderAdapter(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.ttl-seconds:7200}") long ttlSeconds
    ) {
        this.ttl = Duration.ofSeconds(ttlSeconds);

        byte[] decoded = Decoders.BASE64.decode(secretBase64);
        this.key = Keys.hmacShaKeyFor(decoded);
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);

        return Jwts.builder()
                .subject(user.id().asUuid().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            // valida assinatura + exp + estrutura (se exp expirou, lança JwtException)
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}