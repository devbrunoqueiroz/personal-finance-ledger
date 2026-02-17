package com.donyx.lifeops.financeiro.application.ports.user;

import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;

public interface TokenProvider {
    String generateAccessToken(User user);
    boolean isValid(String token);
    String getSubject(UserId userId);
}