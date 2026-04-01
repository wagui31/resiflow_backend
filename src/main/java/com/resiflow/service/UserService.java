package com.resiflow.service;

import com.resiflow.dto.AdminUserActionRequest;
import com.resiflow.dto.CreateAdminRequest;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResidenceService residenceService;
    private final EmailService emailService;
    private final PaymentStatusService paymentStatusService;

    public UserService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final ResidenceService residenceService,
            final EmailService emailService
    ) {
        this(userRepository, passwordEncoder, residenceService, emailService, null);
    }

    @Autowired
    public UserService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final ResidenceService residenceService,
            final EmailService emailService,
            final PaymentStatusService paymentStatusService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.residenceService = residenceService;
        this.emailService = emailService;
        this.paymentStatusService = paymentStatusService;
    }

    @Transactional
    public User createAdmin(final CreateAdminRequest request) {
        validateAdminRequest(request);
        ensureEmailAvailable(request.getEmail());

        Residence residence = residenceService.getRequiredResidence(request.getResidenceId());

        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        user.setEmail(request.getEmail().trim());
        user.setFirstName(normalizeOptionalValue(request.getFirstName()));
        user.setLastName(normalizeOptionalValue(request.getLastName()));
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setResidence(residence);
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setStatutPaiement(StatutPaiement.EN_RETARD);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return userRepository.save(user);
    }

    public List<User> getUsers(final AuthenticatedUser authenticatedUser) {
        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);

        if (actor.role() == UserRole.SUPER_ADMIN) {
            return userRepository.findAll().stream()
                    .map(this::refreshPaymentStatusIfConfigured)
                    .toList();
        }
        if (actor.role() == UserRole.ADMIN) {
            requireResidenceId(actor.residenceId());
            return userRepository.findAllByResidence_Id(actor.residenceId()).stream()
                    .map(this::refreshPaymentStatusIfConfigured)
                    .toList();
        }

        throw new AccessDeniedException("Insufficient role to access users");
    }

    public User getCurrentUser(final AuthenticatedUser authenticatedUser) {
        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);
        return userRepository.findById(actor.userId())
                .map(this::refreshPaymentStatusIfConfigured)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + actor.userId()));
    }

    public User getRequiredUserInResidence(final Long userId, final Long residenceId) {
        requireResidenceId(residenceId);
        return userRepository.findByIdAndResidence_Id(userId, residenceId)
                .orElseThrow(() -> new NoSuchElementException("User not found in residence: " + userId));
    }

    public List<User> getPendingUsers(final AuthenticatedUser authenticatedUser) {
        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);
        if (actor.role() == UserRole.SUPER_ADMIN) {
            return userRepository.findAllByStatus(UserStatus.PENDING).stream()
                    .map(this::refreshPaymentStatusIfConfigured)
                    .toList();
        }
        if (actor.role() == UserRole.ADMIN) {
            requireResidenceId(actor.residenceId());
            return userRepository.findAllByResidence_IdAndStatus(actor.residenceId(), UserStatus.PENDING).stream()
                    .map(this::refreshPaymentStatusIfConfigured)
                    .toList();
        }
        throw new AccessDeniedException("Insufficient role to access pending users");
    }

    @Transactional
    public User approveUser(
            final Long userId,
            final AuthenticatedUser authenticatedUser,
            final AdminUserActionRequest request
    ) {
        User user = getManageableUser(userId, authenticatedUser);
        ensurePendingStatusForApproval(user);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        emailService.sendToUser(
                savedUser.getEmail(),
                "Votre compte ResiFlow est valide",
                buildApprovalBody(request)
        );
        return savedUser;
    }

    @Transactional
    public User rejectUser(
            final Long userId,
            final AuthenticatedUser authenticatedUser,
            final AdminUserActionRequest request
    ) {
        User user = getManageableUser(userId, authenticatedUser);
        ensurePendingStatusForRejection(user);
        user.setStatus(UserStatus.REJECTED);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        emailService.sendToUser(savedUser.getEmail(), "Votre demande a été refusée", buildActionBody("rejete", request));
        return savedUser;
    }

    private void validateAdminRequest(final CreateAdminRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create admin request must not be null");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (request.getResidenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
    }

    private void ensureEmailAvailable(final String email) {
        String normalizedEmail = email.trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already used");
        }
    }

    private AuthenticatedUser requireAuthenticatedUser(final AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user must not be null");
        }
        if (authenticatedUser.userId() == null) {
            throw new IllegalArgumentException("Authenticated user ID must not be null");
        }
        return authenticatedUser;
    }

    private void requireRole(final AuthenticatedUser authenticatedUser, final UserRole role) {
        if (authenticatedUser.role() != role) {
            throw new AccessDeniedException("Insufficient role for this operation");
        }
    }

    private void requireResidenceId(final Long residenceId) {
        if (residenceId == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptionalValue(final String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private User getManageableUser(final Long userId, final AuthenticatedUser authenticatedUser) {
        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);

        if (actor.role() == UserRole.SUPER_ADMIN) {
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
            ensureAdminActionAllowed(actor, user);
            return user;
        }

        if (actor.role() == UserRole.ADMIN) {
            requireResidenceId(actor.residenceId());
            User user = userRepository.findByIdAndResidence_IdForUpdate(userId, actor.residenceId())
                    .orElseThrow(() -> new NoSuchElementException("User not found in residence: " + userId));
            ensureAdminActionAllowed(actor, user);
            return user;
        }

        throw new AccessDeniedException("Insufficient role for this operation");
    }

    private void ensureAdminActionAllowed(final AuthenticatedUser actor, final User user) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Cannot manage a super admin user");
        }
        if (actor.role() == UserRole.ADMIN && user.getRole() == UserRole.ADMIN) {
            throw new AccessDeniedException("Residence admins cannot manage other admins");
        }
    }

    private String buildActionBody(final String action, final AdminUserActionRequest request) {
        if (request != null && !isBlank(request.getComment())) {
            return "Votre demande a ete " + action + ". Commentaire: " + request.getComment().trim();
        }
        return "Votre demande a ete " + action + ".";
    }

    private String buildApprovalBody(final AdminUserActionRequest request) {
        StringBuilder body = new StringBuilder("Votre compte est maintenant valide. Vous pouvez vous connecter a ResiFlow.");
        if (request != null && !isBlank(request.getComment())) {
            body.append(" Commentaire: ").append(request.getComment().trim());
        }
        return body.toString();
    }

    private void ensurePendingStatusForApproval(final User user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("Compte déjà validé");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new IllegalStateException("Compte déjà refusé");
        }
    }

    private void ensurePendingStatusForRejection(final User user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("Compte déjà validé");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new IllegalStateException("Compte déjà refusé");
        }
    }

    private User refreshPaymentStatusIfConfigured(final User user) {
        if (paymentStatusService == null) {
            return user;
        }
        return paymentStatusService.refreshPaymentStatus(user);
    }
}
