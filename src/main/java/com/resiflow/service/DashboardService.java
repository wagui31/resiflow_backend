package com.resiflow.service;

import com.resiflow.dto.DashboardResponse;
import com.resiflow.dto.VoteResponse;
import com.resiflow.entity.Depense;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int MAX_DERNIERS_VOTES = 5;

    private final CagnotteService cagnotteService;
    private final VoteService voteService;
    private final PaiementService paiementService;
    private final DepenseService depenseService;
    private final UserRepository userRepository;

    public DashboardService(
            final CagnotteService cagnotteService,
            final VoteService voteService,
            final PaiementService paiementService,
            final DepenseService depenseService,
            final UserRepository userRepository
    ) {
        this.cagnotteService = cagnotteService;
        this.voteService = voteService;
        this.paiementService = paiementService;
        this.depenseService = depenseService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        BigDecimal soldeCagnotte = cagnotteService.calculerSolde(residenceId, authenticatedUser);
        Long nombreResidents = Long.valueOf(userRepository.findAllByResidence_Id(residenceId).size());
        Long nombreEnRetard = paiementService.countResidentsEnRetard(residenceId, authenticatedUser);
        BigDecimal depensesDuMois = calculateDepensesDuMois(depenseService.getDepensesByResidence(residenceId, authenticatedUser));
        List<VoteResponse> derniersVotes = voteService.getVotesByResidence(residenceId, authenticatedUser).stream()
                .limit(MAX_DERNIERS_VOTES)
                .map(VoteResponse::fromEntity)
                .toList();

        return new DashboardResponse(
                soldeCagnotte,
                nombreResidents,
                nombreEnRetard,
                depensesDuMois,
                derniersVotes
        );
    }

    private BigDecimal calculateDepensesDuMois(final List<Depense> depenses) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        return depenses.stream()
                .filter(depense -> depense.getDateCreation() != null)
                .filter(depense -> !depense.getDateCreation().isBefore(startOfMonth))
                .map(Depense::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
