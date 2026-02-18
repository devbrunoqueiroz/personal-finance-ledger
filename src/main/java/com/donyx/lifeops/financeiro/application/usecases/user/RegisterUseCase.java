package com.donyx.lifeops.financeiro.application.usecases.user;

import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.application.usecases.auth.exceptions.InvalidCredentialsException;
import com.donyx.lifeops.financeiro.domain.user.User;


public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;


    public RegisterUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public String execute(String name, String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            throw new InvalidCredentialsException();
        }
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Email already in use");
        }
        if (userRepository.existsByName(name)) { // se name = username
            throw new IllegalStateException("Username already in use");
        }

        // 2) cria e persiste
        String hashedPassword = passwordHasher.hash(rawPassword);
        User user = User.create(name, email, hashedPassword);

        User saved = userRepository.save(user);

        return tokenProvider.generateAccessToken(saved);
    }
}
