package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.dto.*;

public interface IdentityService {
    UserDto signUp(SignUpInput input);
    JwtAuthResponse signIn(SignInInput input);
}