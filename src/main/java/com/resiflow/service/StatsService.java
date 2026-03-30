package com.resiflow.service;

import com.resiflow.dto.EvolutionCagnotteResponse;
import com.resiflow.dto.StatsResponse;
import com.resiflow.dto.TopPayeurResponse;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.TransactionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ResidenceAccessService residenceAccessService;
    private final PaiementRepository paiementRepository;
    private final TransactionCagnotteRepository transactionCagnotteRepository;

    public StatsService(
            final ResidenceAccessService residenceAccessService,
            final PaiementRepository paiementRepository,
            final TransactionCagnotteRepository transactionCagnotteRepository
    ) {
        this.residenceAccessService = residenceAccessService;
        this.paiementRepository = paiementRepository;
        this.transactionCagnotteRepository = transactionCagnotteRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);

        List<Paiement> paiements = paiementRepository.findAllByResidence_IdOrderByDatePaiementDesc(residenceId);
        List<TransactionCagnotte> transactions =
                transactionCagnotteRepository.findAllByResidence_IdOrderByDateCreationDesc(residenceId);

        BigDecimal totalContributions = sumContributions(transactions);
        BigDecimal totalDepenses = sumDepenses(transactions);

        return new StatsResponse(
                totalContributions,
                totalDepenses,
                totalContributions.subtract(totalDepenses),
                buildTopPayeurs(paiements),
                buildEvolutionCagnotte(transactions)
        );
    }

    private BigDecimal sumContributions(final List<TransactionCagnotte> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TypeTransactionCagnotte.CONTRIBUTION)
                .map(TransactionCagnotte::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDepenses(final List<TransactionCagnotte> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TypeTransactionCagnotte.DEPENSE)
                .map(TransactionCagnotte::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<TopPayeurResponse> buildTopPayeurs(final List<Paiement> paiements) {
        Map<Long, TopPayeurAggregation> aggregations = new LinkedHashMap<>();
        for (Paiement paiement : paiements) {
            Long userId = paiement.getUtilisateur().getId();
            TopPayeurAggregation aggregation = aggregations.computeIfAbsent(
                    userId,
                    ignored -> new TopPayeurAggregation(userId, paiement.getUtilisateur().getEmail())
            );
            aggregations.put(userId, aggregation.add(paiement.getMontantTotal()));
        }

        return aggregations.values().stream()
                .sorted(Comparator.comparing(TopPayeurAggregation::totalPaye).reversed()
                        .thenComparing(TopPayeurAggregation::email))
                .map(aggregation -> new TopPayeurResponse(
                        aggregation.userId(),
                        aggregation.email(),
                        aggregation.totalPaye()
                ))
                .toList();
    }

    private List<EvolutionCagnotteResponse> buildEvolutionCagnotte(final List<TransactionCagnotte> transactions) {
        Map<YearMonth, BigDecimal> deltaByMonth = new LinkedHashMap<>();
        transactions.stream()
                .filter(transaction -> transaction.getDateCreation() != null)
                .sorted(Comparator.comparing(TransactionCagnotte::getDateCreation))
                .forEach(transaction -> {
                    YearMonth month = YearMonth.from(transaction.getDateCreation());
                    BigDecimal delta = transaction.getType() == TypeTransactionCagnotte.CONTRIBUTION
                            ? transaction.getMontant()
                            : transaction.getMontant().negate();
                    deltaByMonth.merge(month, delta, BigDecimal::add);
                });

        BigDecimal runningBalance = BigDecimal.ZERO;
        List<EvolutionCagnotteResponse> evolution = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal> entry : deltaByMonth.entrySet()) {
            runningBalance = runningBalance.add(entry.getValue());
            evolution.add(new EvolutionCagnotteResponse(
                    entry.getKey().format(MONTH_FORMATTER),
                    runningBalance
            ));
        }
        return evolution;
    }

    private record TopPayeurAggregation(Long userId, String email, BigDecimal totalPaye) {

        private TopPayeurAggregation(final Long userId, final String email) {
            this(userId, email, BigDecimal.ZERO);
        }

        private TopPayeurAggregation add(final BigDecimal montant) {
            return new TopPayeurAggregation(userId, email, totalPaye.add(montant));
        }
    }
}
