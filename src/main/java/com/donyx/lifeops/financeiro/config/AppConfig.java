package com.donyx.lifeops.financeiro.config;

import com.donyx.lifeops.financeiro.adapters.outbound.BCryptPasswordHasherAdapter;
import com.donyx.lifeops.financeiro.adapters.outbound.persistance.user.SpringDataJpaRepository;
import com.donyx.lifeops.financeiro.adapters.outbound.persistance.user.UserRepositoryAdapter;
import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.application.usecases.auth.LoginUseCase;
import com.donyx.lifeops.financeiro.application.usecases.user.RegisterUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {

    // ---- Adapters ----

    @Bean
    public UserRepository userRepository(SpringDataJpaRepository jpaRepo) {
        return new UserRepositoryAdapter(jpaRepo);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasherAdapter(12);
    }

    // ---- UseCases ----

    @Bean
    public RegisterUseCase registerUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher, TokenProvider tokenProvider
    ) {
        return new RegisterUseCase(userRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        return new LoginUseCase(userRepository, passwordHasher, tokenProvider);
    }
}
