package com.donyx.lifeops.financeiro.application.usecases.auth;

import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.application.usecases.auth.command.LoginCommand;
import com.donyx.lifeops.financeiro.application.usecases.auth.exceptions.InvalidCredentialsException;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;

public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginUseCase(UserRepository userRepository,
                        PasswordHasher passwordHasher,
                        TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public String execute(LoginCommand command) {
        if (command.email() == null || command.rawPassword() == null) {
            throw new InvalidCredentialsException();
        }

        String normalizedEmail = command.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.status() == UserStatus.DELETED) {
            throw new InvalidCredentialsException();
        }

        if (!passwordHasher.matches(command.rawPassword(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        return tokenProvider.generateAccessToken(user);
    }
}