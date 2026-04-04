package com.resiflow.service;

import com.resiflow.dto.AdminUserActionRequest;
import com.resiflow.dto.CreateAdminRequest;
import com.resiflow.dto.UpdateCurrentUserRequest;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResidenceService residenceService;
    private final PaymentStatusService paymentStatusService;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final ResidenceService residenceService,
            final ApplicationEventPublisher eventPublisher
    ) {
        this(userRepository, passwordEncoder, residenceService, null, eventPublisher);
    }

    @Autowired
    public UserService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final ResidenceService residenceService,
            final PaymentStatusService paymentStatusService,
            final ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.residenceService = residenceService;
        this.paymentStatusService = paymentStatusService;
        this.eventPublisher = eventPublisher;
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
        user.setDateEntreeResidence(now.toLocalDate());
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

    public List<User> getResidenceUsers(final AuthenticatedUser authenticatedUser) {
        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);
        requireResidenceId(actor.residenceId());

        return userRepository.findAllByResidence_IdAndStatus(actor.residenceId(), UserStatus.ACTIVE).stream()
                .map(this::refreshPaymentStatusIfConfigured)
                .sorted(residenceUserComparator(actor.userId()))
                .toList();
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
        eventPublisher.publishEvent(new UserEmailNotificationEvent(
                savedUser.getEmail(),
                "Votre compte ResiFlow est valide",
                buildApprovalBody(request)
        ));
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
        eventPublisher.publishEvent(new UserEmailNotificationEvent(
                savedUser.getEmail(),
                "Votre demande a ete refusee",
                buildActionBody("rejete", request)
        ));
        return savedUser;
    }

    @Transactional
    public User updateResidenceEntryDate(
            final Long userId,
            final AuthenticatedUser authenticatedUser,
            final LocalDate dateEntreeResidence
    ) {
        if (dateEntreeResidence == null) {
            throw new IllegalArgumentException("Date entree residence must not be null");
        }
        User user = getManageableUser(userId, authenticatedUser);
        user.setDateEntreeResidence(dateEntreeResidence);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        if (paymentStatusService != null) {
            return paymentStatusService.refreshPaymentStatus(savedUser);
        }
        return savedUser;
    }

    @Transactional
    public User updateUserRole(
            final Long userId,
            final AuthenticatedUser authenticatedUser,
            final UserRole role
    ) {
        validateRoleUpdateRequest(role);

        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);
        User user = getRoleManageableUser(userId, actor);

        if (user.getRole() == role) {
            return refreshPaymentStatusIfConfigured(user);
        }

        ensureRoleChangeAllowed(user, actor, role);
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return refreshPaymentStatusIfConfigured(savedUser);
    }

    @Transactional
    public User updateCurrentUser(
            final AuthenticatedUser authenticatedUser,
            final UpdateCurrentUserRequest request
    ) {
        validateUpdateCurrentUserRequest(request);

        AuthenticatedUser actor = requireAuthenticatedUser(authenticatedUser);
        User user = userRepository.findByIdForUpdate(actor.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + actor.userId()));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setNumeroImmeuble(normalizeOptionalValue(request.getNumeroImmeuble()));
        user.setCodeLogement(normalizeOptionalValue(request.getCodeLogement()));
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return refreshPaymentStatusIfConfigured(savedUser);
    }

    @Transactional
    public void deleteUser(final Long userId, final AuthenticatedUser authenticatedUser) {
        User user = getManageableUser(userId, authenticatedUser);
        ensureDeletionAllowed(user, authenticatedUser);
        userRepository.delete(user);
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

    private void validateUpdateCurrentUserRequest(final UpdateCurrentUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update current user request must not be null");
        }
        if (isBlank(request.getFirstName())) {
            throw new IllegalArgumentException("First name must not be blank");
        }
        if (isBlank(request.getLastName())) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
    }

    private void validateRoleUpdateRequest(final UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (role != UserRole.ADMIN && role != UserRole.USER) {
            throw new IllegalArgumentException("Role must be ADMIN or USER");
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

    private User getRoleManageableUser(final Long userId, final AuthenticatedUser actor) {
        if (actor.role() == UserRole.SUPER_ADMIN) {
            return userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        }

        if (actor.role() == UserRole.ADMIN) {
            requireResidenceId(actor.residenceId());
            return userRepository.findByIdAndResidence_IdForUpdate(userId, actor.residenceId())
                    .orElseThrow(() -> new NoSuchElementException("User not found in residence: " + userId));
        }

        throw new AccessDeniedException("Insufficient role for this operation");
    }

    private void ensureDeletionAllowed(final User user, final AuthenticatedUser actor) {
        if (user.getId() != null && user.getId().equals(actor.userId())) {
            throw new IllegalStateException("Cannot delete your own account");
        }
        if (user.getRole() == UserRole.ADMIN) {
            Long residenceId = user.getResidenceId();
            if (residenceId != null && userRepository.countByResidence_IdAndRole(residenceId, UserRole.ADMIN) <= 1) {
                throw new IllegalStateException("At least one admin must remain in the residence");
            }
        }
    }

    private void ensureAdminActionAllowed(final AuthenticatedUser actor, final User user) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Cannot manage a super admin user");
        }
        if (actor.role() == UserRole.ADMIN) {
            ensureSameResidence(actor, user);
            if (user.getRole() == UserRole.ADMIN) {
                throw new AccessDeniedException("Residence admins cannot manage other admins");
            }
        }
    }

    private void ensureRoleChangeAllowed(final User user, final AuthenticatedUser actor, final UserRole targetRole) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Cannot manage a super admin user");
        }
        if (user.getId() != null && user.getId().equals(actor.userId())) {
            throw new IllegalStateException("Cannot change your own role");
        }
        if (actor.role() == UserRole.ADMIN) {
            ensureSameResidence(actor, user);
        }
        if (user.getRole() == UserRole.ADMIN && targetRole == UserRole.USER) {
            ensureAnotherAdminRemains(user);
        }
    }

    private void ensureAnotherAdminRemains(final User user) {
        Long residenceId = user.getResidenceId();
        if (residenceId != null && userRepository.countByResidence_IdAndRole(residenceId, UserRole.ADMIN) <= 1) {
            throw new IllegalStateException("At least one admin must remain in the residence");
        }
    }

    private void ensureSameResidence(final AuthenticatedUser actor, final User user) {
        requireResidenceId(actor.residenceId());
        if (user.getResidenceId() == null || !actor.residenceId().equals(user.getResidenceId())) {
            throw new AccessDeniedException("Cannot manage a user from another residence");
        }
    }

    private Comparator<User> residenceUserComparator(final Long currentUserId) {
        return Comparator
                .comparingInt((User user) -> buildResidenceUserRank(user, currentUserId))
                .thenComparing(user -> normalizeForSort(user.getLastName()))
                .thenComparing(user -> normalizeForSort(user.getFirstName()))
                .thenComparing(user -> normalizeForSort(user.getEmail()));
    }

    private int buildResidenceUserRank(final User user, final Long currentUserId) {
        if (user.getId() != null && user.getId().equals(currentUserId)) {
            return 0;
        }
        if (user.getRole() == UserRole.ADMIN) {
            return 1;
        }
        if (user.getStatutPaiement() == StatutPaiement.EN_RETARD) {
            return 2;
        }
        return 3;
    }

    private String normalizeForSort(final String value) {
        return value == null ? "" : value.trim().toLowerCase();
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
            throw new IllegalStateException("Compte deja valide");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new IllegalStateException("Compte deja refuse");
        }
    }

    private void ensurePendingStatusForRejection(final User user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("Compte deja valide");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new IllegalStateException("Compte deja refuse");
        }
    }

    private User refreshPaymentStatusIfConfigured(final User user) {
        if (paymentStatusService == null) {
            return user;
        }
        return paymentStatusService.refreshPaymentStatus(user);
    }
}
