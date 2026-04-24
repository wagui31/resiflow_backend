package com.resiflow.service;

import com.resiflow.dto.CorrectionCagnotteResponse;
import com.resiflow.dto.CreateCorrectionCagnotteRequest;
import com.resiflow.dto.CreateCorrectionCagnotteResponse;
import com.resiflow.entity.CorrectionCagnotte;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.User;
import com.resiflow.repository.CorrectionCagnotteRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorrectionCagnotteService {

    private final CorrectionCagnotteRepository correctionCagnotteRepository;
    private final ResidenceAccessService residenceAccessService;
    private final CagnotteService cagnotteService;
    private final TransactionCagnotteService transactionCagnotteService;

    public CorrectionCagnotteService(
            final CorrectionCagnotteRepository correctionCagnotteRepository,
            final ResidenceAccessService residenceAccessService,
            final CagnotteService cagnotteService,
            final TransactionCagnotteService transactionCagnotteService
    ) {
        this.correctionCagnotteRepository = correctionCagnotteRepository;
        this.residenceAccessService = residenceAccessService;
        this.cagnotteService = cagnotteService;
        this.transactionCagnotteService = transactionCagnotteService;
    }

    @Transactional
    public CreateCorrectionCagnotteResponse createCorrection(
            final Long residenceId,
            final CreateCorrectionCagnotteRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateRequest(request);

        Residence residence = residenceAccessService.getResidenceForAdmin(residenceId, authenticatedUser);
        User actor = residenceAccessService.getRequiredActor(authenticatedUser);
        BigDecimal ancienSolde = cagnotteService.calculerSoldeInterne(residence.getId());
        BigDecimal nouveauSolde = request.getNouveauSolde().setScale(2);
        BigDecimal delta = nouveauSolde.subtract(ancienSolde);

        if (delta.signum() == 0) {
            throw new IllegalArgumentException("Correction delta must not be zero");
        }

        CorrectionCagnotte correction = new CorrectionCagnotte();
        correction.setResidence(residence);
        correction.setAncienSolde(ancienSolde);
        correction.setNouveauSolde(nouveauSolde);
        correction.setDelta(delta);
        correction.setMotif(request.getMotif().trim());
        correction.setCreePar(actor);

        CorrectionCagnotte savedCorrection = correctionCagnotteRepository.save(correction);
        TransactionCagnotte transaction = transactionCagnotteService.createCorrectionTransaction(savedCorrection);
        return CreateCorrectionCagnotteResponse.of(CorrectionCagnotteResponse.fromEntity(savedCorrection), transaction);
    }

    private void validateRequest(final CreateCorrectionCagnotteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create correction cagnotte request must not be null");
        }
        if (request.getNouveauSolde() == null) {
            throw new IllegalArgumentException("Nouveau solde must not be null");
        }
        if (request.getNouveauSolde().signum() < 0) {
            throw new IllegalArgumentException("Nouveau solde must be greater than or equal to zero");
        }
        if (request.getMotif() == null || request.getMotif().trim().isEmpty()) {
            throw new IllegalArgumentException("Motif must not be blank");
        }
    }
}
