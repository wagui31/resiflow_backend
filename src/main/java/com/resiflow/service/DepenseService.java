package com.resiflow.service;

import com.resiflow.dto.CreateDepenseRequest;
import com.resiflow.dto.DepenseContributionUserResponse;
import com.resiflow.entity.CategorieDepense;
import com.resiflow.entity.Depense;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.Residence;
import com.resiflow.entity.StatutDepense;
import com.resiflow.entity.TypeDepense;
import com.resiflow.entity.TypePaiement;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.entity.Vote;
import com.resiflow.repository.DepenseRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final CategorieDepenseService categorieDepenseService;
    private final ResidenceAccessService residenceAccessService;
    private final TransactionCagnotteService transactionCagnotteService;
    private final UserRepository userRepository;
    private final PaiementRepository paiementRepository;

    @Autowired
    public DepenseService(
            final DepenseRepository depenseRepository,
            final CategorieDepenseService categorieDepenseService,
            final ResidenceAccessService residenceAccessService,
            final TransactionCagnotteService transactionCagnotteService,
            final UserRepository userRepository,
            final PaiementRepository paiementRepository
    ) {
        this.depenseRepository = depenseRepository;
        this.categorieDepenseService = categorieDepenseService;
        this.residenceAccessService = residenceAccessService;
        this.transactionCagnotteService = transactionCagnotteService;
        this.userRepository = userRepository;
        this.paiementRepository = paiementRepository;
    }

    public DepenseService(
            final DepenseRepository depenseRepository,
            final CategorieDepenseService categorieDepenseService,
            final ResidenceAccessService residenceAccessService,
            final TransactionCagnotteService transactionCagnotteService
    ) {
        this(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                null,
                null
        );
    }

    @Transactional
    public Depense createDepense(final CreateDepenseRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateRequest(request);

        Residence residence = residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser);
        CategorieDepense categorieDepense = categorieDepenseService.getRequiredCategorieDepense(request.getCategorieId());
        User actor = residenceAccessService.getRequiredActor(authenticatedUser);

        Depense depense = new Depense();
        depense.setResidence(residence);
        depense.setCategorie(categorieDepense);
        depense.setMontant(request.getMontant());
        depense.setTypeDepense(resolveTypeDepense(request));
        depense.setMontantParPersonne(resolveMontantParPersonne(request, residence));
        depense.setDescription(request.getDescription().trim());
        depense.setStatut(StatutDepense.EN_ATTENTE);
        depense.setCreePar(actor);
        depense.setDateCreation(LocalDateTime.now());

        return depenseRepository.save(depense);
    }

    @Transactional
    public Depense approuverDepense(final Long depenseId, final AuthenticatedUser authenticatedUser) {
        Depense depense = getRequiredDepense(depenseId);
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, depense.getResidence().getId());
        if (depense.getStatut() != StatutDepense.EN_ATTENTE) {
            throw new IllegalStateException("Depense is already processed");
        }

        depense.setStatut(StatutDepense.APPROUVEE);
        depense.setValidePar(residenceAccessService.getRequiredActor(authenticatedUser));
        depense.setDateValidation(LocalDateTime.now());
        Depense savedDepense = depenseRepository.save(depense);
        if (savedDepense.getTypeDepense() == TypeDepense.CAGNOTTE) {
            transactionCagnotteService.createDepenseTransaction(savedDepense);
        }
        return savedDepense;
    }

    @Transactional
    public Depense rejeterDepense(final Long depenseId, final AuthenticatedUser authenticatedUser) {
        Depense depense = getRequiredDepense(depenseId);
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, depense.getResidence().getId());
        if (depense.getStatut() != StatutDepense.EN_ATTENTE) {
            throw new IllegalStateException("Depense is already processed");
        }

        depense.setStatut(StatutDepense.REJETEE);
        depense.setValidePar(residenceAccessService.getRequiredActor(authenticatedUser));
        depense.setDateValidation(LocalDateTime.now());
        return depenseRepository.save(depense);
    }

    @Transactional(readOnly = true)
    public List<Depense> getDepensesByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return depenseRepository.findAllByResidence_IdOrderByDateCreationDesc(residenceId);
    }

    @Transactional(readOnly = true)
    public Long countActiveParticipants(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return userRepository.countByResidence_IdAndStatusAndRoleIn(
                residenceId,
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
    }

    @Transactional(readOnly = true)
    public List<DepenseContributionUserResponse> getDepenseContributions(
            final Long depenseId,
            final AuthenticatedUser authenticatedUser
    ) {
        Depense depense = getRequiredDepense(depenseId);
        residenceAccessService.ensureMemberAccessToResidence(authenticatedUser, depense.getResidence().getId());
        if (depense.getTypeDepense() != TypeDepense.PARTAGE) {
            throw new IllegalStateException("Contributions are only available for shared expenses");
        }

        List<User> participants = userRepository.findAllByResidence_IdAndStatusAndRoleIn(
                depense.getResidence().getId(),
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
        Map<Long, BigDecimal> paidByUser = getPaidAmountsByUser(depense.getId());
        BigDecimal montantDu = depense.getMontantParPersonne();

        return participants.stream()
                .map(user -> {
                    BigDecimal montantPaye = paidByUser.getOrDefault(user.getId(), BigDecimal.ZERO);
                    return new DepenseContributionUserResponse(
                            user.getId(),
                            user.getEmail(),
                            montantDu,
                            montantPaye,
                            computeContributionStatus(montantPaye, montantDu)
                    );
                })
                .toList();
    }

    @Transactional
    public Depense createDepenseFromVote(final Vote vote, final java.math.BigDecimal montant, final User actor) {
        if (vote == null) {
            throw new IllegalArgumentException("Vote must not be null");
        }
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Depense montant must be greater than zero");
        }
        if (actor == null) {
            throw new IllegalArgumentException("Actor must not be null");
        }

        Depense depense = new Depense();
        depense.setResidence(vote.getResidence());
        depense.setMontant(montant);
        depense.setTypeDepense(TypeDepense.CAGNOTTE);
        depense.setMontantParPersonne(null);
        depense.setDescription(vote.getTitre().trim());
        depense.setStatut(StatutDepense.EN_ATTENTE);
        depense.setCreePar(actor);
        depense.setDateCreation(LocalDateTime.now());
        return depenseRepository.save(depense);
    }

    public Depense getRequiredDepense(final Long depenseId) {
        if (depenseId == null) {
            throw new IllegalArgumentException("Depense ID must not be null");
        }
        return depenseRepository.findById(depenseId)
                .orElseThrow(() -> new NoSuchElementException("Depense not found: " + depenseId));
    }

    private void validateCreateRequest(final CreateDepenseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create depense request must not be null");
        }
        if (request.getResidenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        if (request.getCategorieId() == null) {
            throw new IllegalArgumentException("Categorie depense ID must not be null");
        }
        if (request.getMontant() == null || request.getMontant().signum() <= 0) {
            throw new IllegalArgumentException("Depense montant must be greater than zero");
        }
        if (request.getTypeDepense() == TypeDepense.PARTAGE
                && request.getMontantParPersonne() != null
                && request.getMontantParPersonne().signum() <= 0) {
            throw new IllegalArgumentException("Montant par personne must be greater than zero");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Depense description must not be blank");
        }
    }

    private TypeDepense resolveTypeDepense(final CreateDepenseRequest request) {
        return request.getTypeDepense() == null ? TypeDepense.CAGNOTTE : request.getTypeDepense();
    }

    private BigDecimal resolveMontantParPersonne(final CreateDepenseRequest request, final Residence residence) {
        TypeDepense typeDepense = resolveTypeDepense(request);
        if (typeDepense == TypeDepense.CAGNOTTE) {
            return null;
        }

        if (request.getMontantParPersonne() != null) {
            return request.getMontantParPersonne();
        }

        long participantsCount = userRepository.countByResidence_IdAndStatusAndRoleIn(
                residence.getId(),
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
        if (participantsCount <= 0) {
            throw new IllegalStateException("No active participants found for residence");
        }

        return request.getMontant().divide(BigDecimal.valueOf(participantsCount), 2, RoundingMode.HALF_UP);
    }

    private Map<Long, BigDecimal> getPaidAmountsByUser(final Long depenseId) {
        Map<Long, BigDecimal> paidByUser = new HashMap<>();
        for (Object[] row : paiementRepository.sumMontantTotalByDepenseAndTypeAndStatusGroupedByUtilisateur(
                depenseId,
                TypePaiement.DEPENSE_PARTAGE,
                PaiementStatus.VALIDATED
        )) {
            paidByUser.put((Long) row[0], (BigDecimal) row[1]);
        }
        return paidByUser;
    }

    private String computeContributionStatus(final BigDecimal montantPaye, final BigDecimal montantDu) {
        if (montantPaye == null || montantPaye.signum() == 0) {
            return "NON_PAYE";
        }
        if (montantPaye.compareTo(montantDu) >= 0) {
            return "PAYE";
        }
        return "PARTIEL";
    }
}
