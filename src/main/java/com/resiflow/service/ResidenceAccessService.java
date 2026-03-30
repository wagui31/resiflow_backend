package com.resiflow.service;

import com.resiflow.entity.Residence;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ResidenceAccessService {

    private final UserRepository userRepository;
    private final ResidenceService residenceService;

    public ResidenceAccessService(final UserRepository userRepository, final ResidenceService residenceService) {
        this.userRepository = userRepository;
        this.residenceService = residenceService;
    }

    public User getRequiredActor(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }

        return userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + authenticatedUser.userId()));
    }

    public Residence getResidenceForAdmin(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        Residence residence = residenceService.getRequiredResidence(residenceId);
        ensureAdminAccessToResidence(authenticatedUser, residence.getId());
        return residence;
    }

    public Residence getResidenceForMember(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        Residence residence = residenceService.getRequiredResidence(residenceId);
        ensureMemberAccessToResidence(authenticatedUser, residence.getId());
        return residence;
    }

    public User getUserForRead(final Long userId, final AuthenticatedUser authenticatedUser) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }

        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        }

        if (authenticatedUser.role() == UserRole.ADMIN) {
            requireResidenceId(authenticatedUser.residenceId());
            return userRepository.findByIdAndResidence_Id(userId, authenticatedUser.residenceId())
                    .orElseThrow(() -> new NoSuchElementException("User not found in residence: " + userId));
        }

        if (!userId.equals(authenticatedUser.userId())) {
            throw new AccessDeniedException("Cannot access another user payments");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    public void ensureAdminAccessToResidence(final AuthenticatedUser authenticatedUser, final Long residenceId) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            return;
        }
        if (authenticatedUser.role() != UserRole.ADMIN) {
            throw new AccessDeniedException("Admin role is required for this operation");
        }
        requireResidenceId(authenticatedUser.residenceId());
        if (!authenticatedUser.residenceId().equals(residenceId)) {
            throw new AccessDeniedException("Cannot access another residence");
        }
    }

    public void ensureMemberAccessToResidence(final AuthenticatedUser authenticatedUser, final Long residenceId) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.role() == UserRole.SUPER_ADMIN) {
            return;
        }
        requireResidenceId(authenticatedUser.residenceId());
        if (!authenticatedUser.residenceId().equals(residenceId)) {
            throw new AccessDeniedException("Cannot access another residence");
        }
    }

    private void requireResidenceId(final Long residenceId) {
        if (residenceId == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
    }
}
