package com.resiflow.dto;

import com.resiflow.entity.User;

public class UserSummaryResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;

    public UserSummaryResponse(
            final Long id,
            final String firstName,
            final String lastName
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static UserSummaryResponse fromEntity(final User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName()
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
}
