package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.dto.*;
import com.dhbw.broker.bff.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final IdentityService identityService;
    
    public UserController(IdentityService identityService) {
        this.identityService = identityService;
    }
    
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UpdateProfileInput input) {
        User updatedUser = identityService.updateProfile(input);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordInput input) {
        identityService.changePassword(input);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/email")
    public ResponseEntity<Void> changeEmail(@Valid @RequestBody ChangeEmailInput input) {
        identityService.changeEmail(input);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountInput input) {
        identityService.deleteAccount(input);
        return ResponseEntity.noContent().build();
    }
}