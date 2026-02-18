package com.donyx.lifeops.financeiro.adapters.inbound.web;

import com.donyx.lifeops.financeiro.application.usecases.auth.LoginUseCase;
import com.donyx.lifeops.financeiro.application.usecases.user.RegisterUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LoginUseCase loginUseCase;

    @MockitoBean
    RegisterUseCase registerUseCase;

    @Test
    @DisplayName("POST /auth/login -> 200 e retorna LoginResponse quando credenciais são válidas")
    void login_ok() throws Exception {
        when(loginUseCase.execute("a@b.com", "123"))
                .thenReturn("token-abc");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content("""
                                {"email":"a@b.com","password":"123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // ajusta os nomes conforme teus records LoginResponse
                .andExpect(jsonPath("$.accessToken").value("token-abc"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200));

        verify(loginUseCase).execute("a@b.com", "123");
    }

    @Test
    @DisplayName("POST /auth/register -> 200 e retorna RegisterResponse quando dados são válidos")
    void register_ok() throws Exception {
        when(registerUseCase.execute("Bruno", "a@b.com", "12345678"))
                .thenReturn("token-xyz");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content("""
                                {"name":"Bruno","email":"a@b.com","password":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // ajusta os nomes conforme teus records RegisterResponse
                .andExpect(jsonPath("$.accessToken").value("token-xyz"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200));

        verify(registerUseCase).execute("Bruno", "a@b.com", "12345678");
    }

    @Test
    @DisplayName("POST /auth/login -> 400 quando request é inválido (@Valid)")
    void login_badRequest_whenInvalid() throws Exception {
        // email inválido + password vazio (depende das anotações no LoginRequest)
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content("""
                                {"email":"nao-email","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register -> 400 quando request é inválido (@Valid)")
    void register_badRequest_whenInvalid() throws Exception {
        // name vazio + email inválido + password vazio (depende das anotações no RegisterRequest)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content("""
                                {"name":"","email":"x","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}