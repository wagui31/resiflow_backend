package com.resiflow.dto;

import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateUserResponse extends UserResponse {

    public CreateUserResponse(
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
        super(
                id,
                email,
                firstName,
                lastName,
                residenceId,
                residenceName,
                residenceCode,
                currency,
                logement,
                dateEntreeResidence,
                role,
                status,
                createdAt,
                updatedAt
        );
    }

    public static CreateUserResponse fromUser(final User user) {
        return new CreateUserResponse(
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
}
