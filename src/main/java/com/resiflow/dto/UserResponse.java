package com.resiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserResponse {

    private final Long id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Long residenceId;
    private final String residenceName;
    private final String residenceCode;
    private final String currency;
    private final LogementSummaryResponse logement;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate dateEntreeResidence;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponse(
            final Long id,
            final String email,
            final String firstName,
            final String lastName,
            final Long residenceId,
            final String residenceName,
            final String residenceCode,
            final String currency,
            final LogementSummaryResponse logement,
            final LocalDate dateEntreeResidence,
            final UserRole role,
            final UserStatus status,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.residenceId = residenceId;
        this.residenceName = residenceName;
        this.residenceCode = residenceCode;
        this.currency = currency;
        this.logement = logement;
        this.dateEntreeResidence = dateEntreeResidence;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserResponse fromUser(final User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getResidenceId(),
                user.getResidence() == null ? null : user.getResidence().getName(),
                user.getResidence() == null ? null : user.getResidence().getCode(),
                user.getResidence() == null ? null : user.getResidence().getCurrency(),
                LogementSummaryResponse.fromEntity(user.getLogement()),
                user.getDateEntreeResidence(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public String getResidenceName() {
        return residenceName;
    }

    public String getResidenceCode() {
        return residenceCode;
    }

    public String getCurrency() {
        return currency;
    }

    public LogementSummaryResponse getLogement() {
        return logement;
    }

    public LocalDate getDateEntreeResidence() {
        return dateEntreeResidence;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
