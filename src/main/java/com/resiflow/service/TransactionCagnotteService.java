package com.resiflow.service;

import com.resiflow.entity.CorrectionCagnotte;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Paiement;
import com.resiflow.entity.TransactionCagnotte;
import com.resiflow.entity.TypeTransactionCagnotte;
import com.resiflow.repository.TransactionCagnotteRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionCagnotteService {

    private final TransactionCagnotteRepository transactionCagnotteRepository;

    public TransactionCagnotteService(final TransactionCagnotteRepository transactionCagnotteRepository) {
        this.transactionCagnotteRepository = transactionCagnotteRepository;
    }

    public TransactionCagnotte createContributionTransaction(final Paiement paiement) {
        if (transactionCagnotteRepository.existsByTypeAndReferenceId(TypeTransactionCagnotte.CONTRIBUTION, paiement.getId())) {
            throw new IllegalStateException("Contribution transaction already exists for paiement " + paiement.getId());
        }
        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setResidence(paiement.getResidence());
        transaction.setLogement(paiement.getLogement());
        transaction.setType(TypeTransactionCagnotte.CONTRIBUTION);
        transaction.setMontant(paiement.getMontantTotal());
        transaction.setReferenceId(paiement.getId());
        return transactionCagnotteRepository.save(transaction);
    }

    public TransactionCagnotte createDepenseTransaction(final Depense depense) {
        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setResidence(depense.getResidence());
        transaction.setLogement(null);
        transaction.setType(TypeTransactionCagnotte.DEPENSE);
        transaction.setMontant(depense.getMontant());
        transaction.setReferenceId(depense.getId());
        return transactionCagnotteRepository.save(transaction);
    }

    public TransactionCagnotte createCorrectionTransaction(final CorrectionCagnotte correction) {
        if (correction == null) {
            throw new IllegalArgumentException("Correction must not be null");
        }
        if (correction.getDelta() == null || correction.getDelta().signum() == 0) {
            throw new IllegalArgumentException("Correction delta must not be zero");
        }

        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setResidence(correction.getResidence());
        transaction.setLogement(null);
        transaction.setType(TypeTransactionCagnotte.CORRECTION);
        transaction.setMontant(correction.getDelta());
        transaction.setReferenceId(correction.getId());
        return transactionCagnotteRepository.save(transaction);
    }
}
