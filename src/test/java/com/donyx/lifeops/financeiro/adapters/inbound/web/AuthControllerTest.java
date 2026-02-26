package com.donyx.lifeops.financeiro.adapters.inbound.web;

import com.donyx.lifeops.financeiro.adapters.inbound.web.auth.AuthController;
import com.donyx.lifeops.financeiro.application.usecases.auth.LoginUseCase;
import com.donyx.lifeops.financeiro.application.usecases.auth.command.LoginCommand;
import com.donyx.lifeops.financeiro.application.usecases.user.RegisterUseCase;
import com.donyx.lifeops.financeiro.config.SecurityFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityFilter.class
        )
)
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
        LoginCommand loginCommand = new LoginCommand("a@b.com", "123");
        when(loginUseCase.execute(loginCommand))
                .thenReturn("token-abc");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@b.com","password":"123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("token-abc"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200));

        verify(loginUseCase).execute(loginCommand);
    }

    @Test
    @DisplayName("POST /auth/register -> 200 e retorna RegisterResponse quando dados são válidos")
    void register_ok() throws Exception {
        when(registerUseCase.execute("Bruno", "a@b.com", "12345678"))
                .thenReturn("token-xyz");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bruno","email":"a@b.com","password":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("token-xyz"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200));

        verify(registerUseCase).execute("Bruno", "a@b.com", "12345678");
    }

    @Test
    @DisplayName("POST /auth/login -> 400 quando request é inválido (@Valid)")
    void login_badRequest_whenInvalid() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nao-email","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register -> 400 quando request é inválido (@Valid)")
    void register_badRequest_whenInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"x","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}