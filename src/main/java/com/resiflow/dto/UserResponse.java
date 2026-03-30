package com.resiflow.dto;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.UserStatus;
import java.time.LocalDateTime;

public class UserResponse {

    private final Long id;
    private final String email;
    private final Long residenceId;
    private final String residenceCode;
    private final String numeroImmeuble;
    private final String codeLogement;
    private final UserRole role;
    private final UserStatus status;
    private final StatutPaiement statutPaiement;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponse(
            final Long id,
            final String email,
            final Long residenceId,
            final String residenceCode,
            final String numeroImmeuble,
            final String codeLogement,
            final UserRole role,
            final UserStatus status,
            final StatutPaiement statutPaiement,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.residenceId = residenceId;
        this.residenceCode = residenceCode;
        this.numeroImmeuble = numeroImmeuble;
        this.codeLogement = codeLogement;
        this.role = role;
        this.status = status;
        this.statutPaiement = statutPaiement;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserResponse(
            final Long id,
            final String email,
            final Long residenceId,
            final String residenceCode,
            final String numeroImmeuble,
            final String codeLogement,
            final UserRole role,
            final UserStatus status,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ) {
        this(id, email, residenceId, residenceCode, numeroImmeuble, codeLogement, role, status, null, createdAt, updatedAt);
    }

    public static UserResponse fromUser(final User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getResidenceId(),
                user.getResidence() == null ? null : user.getResidence().getCode(),
                user.getNumeroImmeuble(),
                user.getCodeLogement(),
                user.getRole(),
                user.getStatus(),
                user.getStatutPaiement(),
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

    public Long getResidenceId() {
        return residenceId;
    }

    public String getResidenceCode() {
        return residenceCode;
    }

    public String getNumeroImmeuble() {
        return numeroImmeuble;
    }

    public String getCodeLogement() {
        return codeLogement;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public StatutPaiement getStatutPaiement() {
        return statutPaiement;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
