package com.resiflow.dto;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.UserStatus;
import java.time.LocalDateTime;

public class CreateUserResponse extends UserResponse {

    public CreateUserResponse(
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
        super(
                id,
                email,
                residenceId,
                residenceCode,
                numeroImmeuble,
                codeLogement,
                role,
                status,
                statutPaiement,
                createdAt,
                updatedAt
        );
    }

    public static CreateUserResponse fromUser(final User user) {
        return new CreateUserResponse(
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
}
