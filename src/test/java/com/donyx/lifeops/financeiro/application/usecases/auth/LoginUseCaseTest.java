package com.donyx.lifeops.financeiro.application.usecases.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.application.usecases.auth.command.LoginCommand;
import com.donyx.lifeops.financeiro.application.usecases.exceptions.InvalidCredentialsException;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class LoginUseCaseTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private TokenProvider tokenProvider;

    private LoginUseCase useCase;

    private LoginCommand command;
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        tokenProvider = mock(TokenProvider.class);
        useCase = new LoginUseCase(userRepository, passwordHasher, tokenProvider);
        command = new LoginCommand("a@b.com", "12345678");

    }

    @Test
    @DisplayName("execute -> retorna token quando credenciais são válidas (email normalizado)")
    void execute_ok_returnsToken() {
        String email = "  A@B.COM ";
        String normalized = "a@b.com";
        String rawPassword = "12345678";

        User user = mock(User.class);
        when(userRepository.findByEmail(normalized)).thenReturn(Optional.of(user));
        when(user.status()).thenReturn(UserStatus.ACTIVE);
        when(user.passwordHash()).thenReturn("HASH");

        when(passwordHasher.matches(rawPassword, "HASH")).thenReturn(true);
        when(tokenProvider.generateAccessToken(user)).thenReturn("token-abc");

        String token = useCase.execute(new LoginCommand(email, rawPassword));

        assertEquals("token-abc", token);

        verify(userRepository).findByEmail(normalized);
        verify(user).status();
        verify(user).passwordHash();
        verify(passwordHasher).matches(rawPassword, "HASH");
        verify(tokenProvider).generateAccessToken(user);
        verifyNoMoreInteractions(userRepository, passwordHasher, tokenProvider, user);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando email é null")
    void execute_emailNull_throws() {
        LoginCommand nullEmailCommand = new LoginCommand(null, "12345678");
        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(nullEmailCommand));

        verifyNoInteractions(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando password é null")
    void execute_passwordNull_throws() {
        LoginCommand nullPWCommand = new LoginCommand("a@b.com", null);
        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(nullPWCommand));

        verifyNoInteractions(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando usuário não existe")
    void execute_userNotFound_throws() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        LoginCommand invalidCommand = new LoginCommand("a@b.com", "12345678");
        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(invalidCommand));

        verify(userRepository).findByEmail("a@b.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando usuário está DELETED")
    void execute_userDeleted_throws() {
        User user = mock(User.class);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(user.status()).thenReturn(UserStatus.DELETED);

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(command));

        verify(userRepository).findByEmail("a@b.com");
        verify(user).status();
        verifyNoMoreInteractions(userRepository, user);
        verifyNoInteractions(passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando senha não confere")
    void execute_passwordMismatch_throws() {
        User user = mock(User.class);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(user.status()).thenReturn(UserStatus.ACTIVE);
        when(user.passwordHash()).thenReturn("HASH");

        when(passwordHasher.matches("wrong", "HASH")).thenReturn(false);

        LoginCommand wrongCommand = new LoginCommand("a@b.com", "wrong");
        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(wrongCommand));

        verify(userRepository).findByEmail("a@b.com");
        verify(user).status();
        verify(user).passwordHash();
        verify(passwordHasher).matches("wrong", "HASH");
        verifyNoMoreInteractions(userRepository, passwordHasher, user);
        verifyNoInteractions(tokenProvider);
    }
}