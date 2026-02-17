package com.donyx.lifeops.financeiro.application.usecases.user;

import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.domain.user.User;

import java.time.Instant;

public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;


    public RegisterUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public void execute(String name, String email, String rawPassword) {
        String hashedPassword = passwordHasher.hash(rawPassword);
        User user = User.create(name, email, hashedPassword, Instant.now());
        userRepository.save(user);
    }
}
