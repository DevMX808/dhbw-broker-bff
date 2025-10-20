package com.dhbw.broker.bff.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "users_email_key", columnNames = "email")
)
public class User {

    @Getter
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    @UuidGenerator
    private UUID userId;

    @Setter
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Getter
    @Setter
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private Status status;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Object getEmail() {
        return email;
    }

    public Object getFirstName() {
        return firstName;
    }

    public Object getLastName() {
        return lastName;
    }

    public void setId(UUID uuid) {
        this.userId = uuid;
    }

    public void setFirstName(@NotBlank @Size(max = 120) String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(@NotBlank @Size(max = 120) String lastName) {
        this.lastName = lastName;
    }


    public void setAdmin(boolean b) {
        if (b) {
            this.role = Role.ADMIN;
        } else {
            this.role = Role.USER;
        }
    }

    public void setHashedPassword(String encode) {
        this.passwordHash = encode;
    }

    public String getHashedPassword() {
        return this.passwordHash;
    }

    public Object getId() {
        return this.userId;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

}