package com.resiflow.service;

import com.resiflow.dto.ExpenseCategoryCountResponse;
import com.resiflow.dto.EvolutionCagnotteResponse;
import com.resiflow.dto.ResidenceExpenseCategoryStatsResponse;
import com.resiflow.dto.ResidencePaymentHousingStatsResponse;
import com.resiflow.dto.StatsResponse;
import com.resiflow.dto.TopPayeurResponse;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.PaiementStatus;
import com.resiflow.entity.StatutPaiement;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.entity.TypePaiement;
import com.resiflow.repository.DepenseRepository;
import com.resiflow.repository.LogementRepository;
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
    private final LogementRepository logementRepository;
    private final DepenseRepository depenseRepository;
    private final PaymentStatusService paymentStatusService;

    public StatsService(
            final ResidenceAccessService residenceAccessService,
            final PaiementRepository paiementRepository,
            final TransactionCagnotteRepository transactionCagnotteRepository,
            final LogementRepository logementRepository,
            final DepenseRepository depenseRepository,
            final PaymentStatusService paymentStatusService
    ) {
        this.residenceAccessService = residenceAccessService;
        this.paiementRepository = paiementRepository;
        this.transactionCagnotteRepository = transactionCagnotteRepository;
        this.logementRepository = logementRepository;
        this.depenseRepository = depenseRepository;
        this.paymentStatusService = paymentStatusService;
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);

        List<Paiement> paiements =
                paiementRepository.findAllByResidence_IdAndStatusAndTypePaiementAndIsDeletedFalseOrderByDatePaiementDesc(
                        residenceId,
                        PaiementStatus.VALIDATED,
                        TypePaiement.CAGNOTTE
                );
        List<TransactionCagnotte> transactions =
                transactionCagnotteRepository.findAllByResidence_IdOrderByDateCreationDesc(residenceId);

        BigDecimal totalContributions = sumContributions(transactions);
        BigDecimal totalDepenses = sumDepenses(transactions);
        BigDecimal totalCorrections = sumCorrections(transactions);

        return new StatsResponse(
                totalContributions,
                totalDepenses,
                totalContributions.subtract(totalDepenses).add(totalCorrections),
                buildTopPayeurs(paiements),
                buildEvolutionCagnotte(transactions)
        );
    }

    @Transactional(readOnly = true)
    public ResidencePaymentHousingStatsResponse getPaiementLogementStats(
            final Long residenceId,
            final AuthenticatedUser authenticatedUser
    ) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);

        List<Logement> logementsActifs =
                logementRepository.findAllByResidence_IdAndActiveOrderByNumeroAsc(residenceId, Boolean.TRUE);
        long logementsEnRetard = logementsActifs.stream()
                .filter(logement -> paymentStatusService.calculateStatus(logement) == StatutPaiement.EN_RETARD)
                .count();
        long totalLogementsActifs = logementsActifs.size();

        return new ResidencePaymentHousingStatsResponse(
                residenceId,
                totalLogementsActifs,
                totalLogementsActifs - logementsEnRetard,
                logementsEnRetard
        );
    }

    @Transactional(readOnly = true)
    public ResidenceExpenseCategoryStatsResponse getDepenseCategoryStats(
            final Long residenceId,
            final AuthenticatedUser authenticatedUser
    ) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);

        List<ExpenseCategoryCountResponse> categories =
                depenseRepository.findAllByResidence_IdAndIsDeletedFalseOrderByDateCreationDesc(residenceId).stream()
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        this::buildCategoryKey,
                                        java.util.stream.Collectors.counting()
                                )
                        )
                        .entrySet()
                        .stream()
                        .sorted(Comparator.<Map.Entry<CategoryAggregationKey, Long>, Long>comparing(Map.Entry::getValue)
                                .reversed()
                                .thenComparing(entry -> entry.getKey().categorieNom()))
                        .map(entry -> new ExpenseCategoryCountResponse(
                                entry.getKey().categorieId(),
                                entry.getKey().categorieNom(),
                                entry.getValue()
                        ))
                        .toList();

        return new ResidenceExpenseCategoryStatsResponse(residenceId, categories);
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

    private BigDecimal sumCorrections(final List<TransactionCagnotte> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TypeTransactionCagnotte.CORRECTION)
                .map(TransactionCagnotte::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<TopPayeurResponse> buildTopPayeurs(final List<Paiement> paiements) {
        Map<Long, TopPayeurAggregation> aggregations = new LinkedHashMap<>();
        for (Paiement paiement : paiements) {
            Long logementId = paiement.getLogement().getId();
            TopPayeurAggregation aggregation = aggregations.computeIfAbsent(
                    logementId,
                    ignored -> new TopPayeurAggregation(
                            logementId,
                            buildLogementLabel(paiement)
                    )
            );
            aggregations.put(logementId, aggregation.add(paiement.getMontantTotal()));
        }

        return aggregations.values().stream()
                .sorted(Comparator.comparing(TopPayeurAggregation::totalPaye).reversed()
                        .thenComparing(TopPayeurAggregation::label))
                .map(aggregation -> new TopPayeurResponse(
                        aggregation.logementId(),
                        aggregation.label(),
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
                    BigDecimal delta = switch (transaction.getType()) {
                        case CONTRIBUTION -> transaction.getMontant();
                        case DEPENSE -> transaction.getMontant().negate();
                        case CORRECTION -> transaction.getMontant();
                    };
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

    private String buildLogementLabel(final Paiement paiement) {
        String numero = paiement.getLogement().getNumero();
        String immeuble = paiement.getLogement().getImmeuble();
        return immeuble == null || immeuble.isBlank() ? numero : immeuble + " - " + numero;
    }

    private CategoryAggregationKey buildCategoryKey(final Depense depense) {
        if (depense.getCategorie() == null) {
            return new CategoryAggregationKey(null, "Sans categorie");
        }
        return new CategoryAggregationKey(depense.getCategorie().getId(), depense.getCategorie().getNom());
    }

    private record TopPayeurAggregation(Long logementId, String label, BigDecimal totalPaye) {

        private TopPayeurAggregation(final Long logementId, final String label) {
            this(logementId, label, BigDecimal.ZERO);
        }

        private TopPayeurAggregation add(final BigDecimal montant) {
            return new TopPayeurAggregation(logementId, label, totalPaye.add(montant));
        }
    }

    private record CategoryAggregationKey(Long categorieId, String categorieNom) {
    }
}
