package com.resiflow.service;

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
        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setResidence(paiement.getResidence());
        transaction.setUser(paiement.getUtilisateur());
        transaction.setType(TypeTransactionCagnotte.CONTRIBUTION);
        transaction.setMontant(paiement.getMontantTotal());
        transaction.setReferenceId(paiement.getId());
        return transactionCagnotteRepository.save(transaction);
    }

    public TransactionCagnotte createDepenseTransaction(final Depense depense) {
        TransactionCagnotte transaction = new TransactionCagnotte();
        transaction.setResidence(depense.getResidence());
        transaction.setUser(null);
        transaction.setType(TypeTransactionCagnotte.DEPENSE);
        transaction.setMontant(depense.getMontant());
        transaction.setReferenceId(depense.getId());
        return transactionCagnotteRepository.save(transaction);
    }
}
