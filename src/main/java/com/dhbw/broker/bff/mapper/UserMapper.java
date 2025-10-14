package com.dhbw.broker.bff.mapper;

import com.dhbw.broker.bff.dto.UserDto;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User u) {
        UserDto d = new UserDto();
        d.setId(u.getId() != null ? u.getId().toString() : null);
        d.setEmail(u.getEmail());
        d.setFirstName(u.getFirstName());
        d.setLastName(u.getLastName());
        d.setAdmin(u.isAdmin());
        return d;
    }

    public User fromSignUp(SignUpInput in, String passwordHash) {
        User u = new User();
        u.setEmail(in.getEmail());
        u.setFirstName(in.getFirstName());
        u.setLastName(in.getLastName());
        u.setHashedPassword(passwordHash);
        u.setAdmin(false);
        return u;
    }
}