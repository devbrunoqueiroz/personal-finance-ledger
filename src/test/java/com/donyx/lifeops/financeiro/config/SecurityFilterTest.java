package com.donyx.lifeops.financeiro.config;

import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserRole;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_doesNotAuthenticate_andContinuesChain() throws Exception {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        UserRepository userRepository = mock(UserRepository.class);
        FilterChain chain = mock(FilterChain.class);

        SecurityFilter filter = new SecurityFilter(tokenProvider, userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(tokenProvider, userRepository);
    }

    @Test
    void bearerToken_valid_authenticatesUser_andContinuesChain() throws Exception {

        TokenProvider tokenProvider = mock(TokenProvider.class);
        UserRepository userRepository = mock(UserRepository.class);
        FilterChain chain = mock(FilterChain.class);

        SecurityFilter filter = new SecurityFilter(tokenProvider, userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer abc.def.ghi");

        UUID userId = UUID.randomUUID();

        when(tokenProvider.getSubject("abc.def.ghi"))
                .thenReturn(userId.toString());

        Instant now = Instant.now();

        User domainUser = User.rehydrate(
                UserId.of(userId),
                "Bruno",
                "a@b.com",
                "HASH",
                null,
                now,
                UserStatus.ACTIVE,
                Set.of(UserRole.USER)
        );

        when(userRepository.findById(UserId.of(userId)))
                .thenReturn(Optional.of(domainUser));

        filter.doFilter(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
        assertTrue(auth.isAuthenticated());

        Object principal = auth.getPrincipal();
        assertTrue(principal instanceof org.springframework.security.core.userdetails.User);

        var springUser = (org.springframework.security.core.userdetails.User) principal;

        // AGORA username = UUID
        assertEquals(userId.toString(), springUser.getUsername());

        assertTrue(
                springUser.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );

        verify(chain).doFilter(request, response);
        verify(tokenProvider).getSubject("abc.def.ghi");
        verify(userRepository).findById(UserId.of(userId));
    }

    @Test
    void bearerToken_invalid_throwsException() throws Exception {

        TokenProvider tokenProvider = mock(TokenProvider.class);
        UserRepository userRepository = mock(UserRepository.class);
        FilterChain chain = mock(FilterChain.class);

        SecurityFilter filter = new SecurityFilter(tokenProvider, userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer bad");

        when(tokenProvider.getSubject("bad"))
                .thenThrow(new RuntimeException("invalid token"));

        assertThrows(RuntimeException.class, () ->
                filter.doFilter(request, response, chain)
        );

        verifyNoInteractions(userRepository);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void whenAlreadyAuthenticated_filterDoesNothing_andContinuesChain() throws Exception {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        UserRepository userRepository = mock(UserRepository.class);
        FilterChain chain = mock(FilterChain.class);

        SecurityFilter filter = new SecurityFilter(tokenProvider, userRepository);

        // seta um auth prévio
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", null, java.util.List.of())
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer abc");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(tokenProvider, userRepository);
    }
}