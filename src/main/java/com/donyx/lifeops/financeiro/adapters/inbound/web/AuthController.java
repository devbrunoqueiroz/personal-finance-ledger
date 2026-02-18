package com.donyx.lifeops.financeiro.adapters.inbound.web;

import com.donyx.lifeops.financeiro.adapters.inbound.web.dto.LoginRequest;
import com.donyx.lifeops.financeiro.adapters.inbound.web.dto.LoginResponse;
import com.donyx.lifeops.financeiro.adapters.inbound.web.dto.RegisterRequest;
import com.donyx.lifeops.financeiro.adapters.inbound.web.dto.RegisterResponse;
import com.donyx.lifeops.financeiro.application.usecases.auth.LoginUseCase;
import com.donyx.lifeops.financeiro.application.usecases.user.RegisterUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;

    public AuthController(LoginUseCase loginUseCase, RegisterUseCase registerUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        String token = loginUseCase.execute(req.email(), req.password());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", 7200));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest req){
        String token = registerUseCase.execute(req.name(), req.email(), req.password());
        return ResponseEntity.ok(new RegisterResponse(token,"Bearer", 7200));
    }
}
