package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import java.time.LocalDate;

public class ResidenceViewResidentResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final UserRole role;
    private final UserStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateEntreeResidence;

    public ResidenceViewResidentResponse(
            final Long id,
            final String firstName,
            final String lastName,
            final String email,
            final UserRole role,
            final UserStatus status,
            final LocalDate dateEntreeResidence
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.dateEntreeResidence = dateEntreeResidence;
    }

    public static ResidenceViewResidentResponse fromUser(final User user) {
        return new ResidenceViewResidentResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getDateEntreeResidence()
        );
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDate getDateEntreeResidence() {
        return dateEntreeResidence;
    }
}
