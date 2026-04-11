package com.resiflow.dto;

import com.resiflow.entity.User;

public class ExpenseUserSummaryResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String fullName;

    public ExpenseUserSummaryResponse(
            final Long id,
            final String firstName,
            final String lastName,
            final String fullName
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
    }

    public static ExpenseUserSummaryResponse fromUser(final User user) {
        if (user == null) {
            return null;
        }
        return new ExpenseUserSummaryResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                buildFullName(user.getFirstName(), user.getLastName(), user.getEmail())
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

    public String getFullName() {
        return fullName;
    }

    private static String buildFullName(
            final String firstName,
            final String lastName,
            final String fallback
    ) {
        String normalizedFirstName = firstName == null ? "" : firstName.trim();
        String normalizedLastName = lastName == null ? "" : lastName.trim();
        String fullName = (normalizedFirstName + " " + normalizedLastName).trim();
        return fullName.isEmpty() ? fallback : fullName;
    }
}
