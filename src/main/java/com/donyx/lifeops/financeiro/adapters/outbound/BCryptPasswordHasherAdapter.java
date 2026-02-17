package com.donyx.lifeops.financeiro.adapters.outbound;

import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordHasherAdapter implements PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasherAdapter(int strength) {
        this.encoder = new BCryptPasswordEncoder(strength);
    }

    public BCryptPasswordHasherAdapter() {
        this.encoder = new BCryptPasswordEncoder(); // default 10
    }

    @Override
    public String hash(String raw) {
        return encoder.encode(raw);
    }

    @Override
    public boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}