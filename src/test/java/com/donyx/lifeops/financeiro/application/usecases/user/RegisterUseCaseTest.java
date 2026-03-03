package com.donyx.lifeops.financeiro.application.usecases.user;

import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.application.usecases.exceptions.EmailAlreadyInUseException;
import com.donyx.lifeops.financeiro.application.usecases.exceptions.InvalidCredentialsException;
import com.donyx.lifeops.financeiro.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegisterUseCaseTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private TokenProvider tokenProvider;

    private RegisterUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        tokenProvider = mock(TokenProvider.class);
        useCase = new RegisterUseCase(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> retorna token e salva usuário quando dados são válidos")
    void execute_ok_returnsToken() {
        // arrange
        String name = "Bruno";
        String email = "  A@B.COM ";
        String rawPassword = "12345678";
        String normalizedEmail = "a@b.com";

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(passwordHasher.hash(rawPassword)).thenReturn("HASH");
        // devolve o mesmo usuário que recebeu (pra simplificar)
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("token-xyz");

        // act
        String token = useCase.execute(name, email, rawPassword);

        // assert
        assertEquals("token-xyz", token);

        verify(userRepository).existsByEmail(normalizedEmail);
        verify(passwordHasher).hash(rawPassword);
        verify(userRepository).save(any(User.class));
        verify(tokenProvider).generateAccessToken(any(User.class));
        verifyNoMoreInteractions(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando email é null")
    void execute_emailNull_throws() {
        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute("Bruno", null, "12345678"));

        verifyNoInteractions(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança InvalidCredentialsException quando password é null")
    void execute_passwordNull_throws() {
        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute("Bruno", "a@b.com", null));

        verifyNoInteractions(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("execute -> lança EmailAlreadyInUseException quando email já está em uso (checando email normalizado)")
    void execute_emailAlreadyInUse_throws() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        EmailAlreadyInUseException ex = assertThrows(EmailAlreadyInUseException.class,
                () -> useCase.execute("Bruno", " A@B.COM ", "12345678"));

        assertEquals("The email A@B.COM is already in use.", ex.getMessage());

        verify(userRepository).existsByEmail("a@b.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordHasher, tokenProvider);
    }

}