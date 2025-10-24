package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.dto.JwtAuthResponse;
import com.dhbw.broker.bff.dto.SignInInput;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.domain.User;

public interface IdentityService {
    JwtAuthResponse register(SignUpInput input);
    JwtAuthResponse login(SignInInput input);
    User getCurrentUser();
}