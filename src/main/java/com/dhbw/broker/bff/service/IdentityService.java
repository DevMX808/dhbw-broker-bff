package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.dto.*;
import com.dhbw.broker.bff.domain.User;

public interface IdentityService {
    JwtAuthResponse register(SignUpInput input);
    JwtAuthResponse login(SignInInput input);
    User getCurrentUser();
    
   
    User updateProfile(UpdateProfileInput input);
    void changePassword(ChangePasswordInput input);
    void changeEmail(ChangeEmailInput input);
    void deleteAccount(DeleteAccountInput input);
}