package com.resiflow.service;

import com.resiflow.dto.CreateDepenseRequest;
import com.resiflow.entity.Logement;
import com.resiflow.entity.CategorieDepense;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeDepense;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.DepenseRepository;
import com.resiflow.repository.LogementRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepenseServiceTest {

    @Test
    void createDepenseRequiresCategorieId() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository,
                logementRepository
        );

        CreateDepenseRequest request = new CreateDepenseRequest();
        request.setResidenceId(7L);
        request.setMontant(new BigDecimal("250.50"));
        request.setDescription("Remplacement ampoules hall");
        request.setTypeDepense(TypeDepense.CAGNOTTE);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        assertThatThrownBy(() -> depenseService.createDepense(request, authenticatedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Categorie depense ID must not be null");

        verify(categorieDepenseService, never()).getRequiredCategorieDepense(any());
        verify(depenseRepository, never()).save(any());
    }

    @Test
    void createDepenseCreatesPendingDepenseForPartage() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository,
                logementRepository
        );

        CreateDepenseRequest request = new CreateDepenseRequest();
        request.setResidenceId(7L);
        request.setCategorieId(5L);
        request.setMontant(new BigDecimal("300.00"));
        request.setDescription("Frais partages");
        request.setTypeDepense(TypeDepense.PARTAGE);
        request.setMontantParPersonne(new BigDecimal("100.00"));

        Residence residence = new Residence();
        residence.setId(7L);

        User actor = new User();
        actor.setId(2L);

        CategorieDepense categorie = new CategorieDepense();
        categorie.setId(5L);
        categorie.setNom("Entretien");

        when(residenceAccessService.getResidenceForAdmin(eq(7L), any(AuthenticatedUser.class))).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(any())).thenReturn(actor);
        when(categorieDepenseService.getRequiredCategorieDepense(5L)).thenReturn(categorie);
        when(logementRepository.countByResidence_IdAndActiveTrue(7L)).thenReturn(3L);
        when(depenseRepository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);
        Depense result = depenseService.createDepense(request, authenticatedUser);

        assertThat(result.getCategorie()).isEqualTo(categorie);
        assertThat(result.getTypeDepense()).isEqualTo(TypeDepense.PARTAGE);
        assertThat(result.getMontantParPersonne()).isEqualByComparingTo("100.00");
        assertThat(result.getCreePar()).isEqualTo(actor);
        assertThat(result.getResidence()).isEqualTo(residence);
    }

    @Test
    void createDepenseAllowsPartageWithoutCategorieId() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository,
                logementRepository
        );

        CreateDepenseRequest request = new CreateDepenseRequest();
        request.setResidenceId(7L);
        request.setMontant(new BigDecimal("300.00"));
        request.setDescription("Frais partages");
        request.setTypeDepense(TypeDepense.PARTAGE);
        request.setMontantParPersonne(new BigDecimal("100.00"));

        Residence residence = new Residence();
        residence.setId(7L);

        User actor = new User();
        actor.setId(2L);

        when(residenceAccessService.getResidenceForAdmin(eq(7L), any(AuthenticatedUser.class))).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(any())).thenReturn(actor);
        when(depenseRepository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = depenseService.createDepense(
                request,
                new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN)
        );

        assertThat(result.getCategorie()).isNull();
        assertThat(result.getTypeDepense()).isEqualTo(TypeDepense.PARTAGE);
        assertThat(result.getMontantParPersonne()).isEqualByComparingTo("100.00");
        verify(categorieDepenseService, never()).getRequiredCategorieDepense(any());
    }

    @Test
    void createDepenseComputesSharedAmountPerActiveLogement() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository,
                logementRepository
        );

        CreateDepenseRequest request = new CreateDepenseRequest();
        request.setResidenceId(7L);
        request.setCategorieId(5L);
        request.setMontant(new BigDecimal("120.00"));
        request.setDescription("Menage commun");
        request.setTypeDepense(TypeDepense.PARTAGE);

        Residence residence = new Residence();
        residence.setId(7L);

        User actor = new User();
        actor.setId(2L);

        CategorieDepense categorie = new CategorieDepense();
        categorie.setId(5L);
        categorie.setNom("Entretien");

        when(residenceAccessService.getResidenceForAdmin(eq(7L), any(AuthenticatedUser.class))).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(any())).thenReturn(actor);
        when(categorieDepenseService.getRequiredCategorieDepense(5L)).thenReturn(categorie);
        when(logementRepository.countByResidence_IdAndActiveTrue(7L)).thenReturn(4L);
        when(depenseRepository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = depenseService.createDepense(
                request,
                new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN)
        );

        assertThat(result.getMontantParPersonne()).isEqualByComparingTo("30.00");
        verify(logementRepository).countByResidence_IdAndActiveTrue(7L);
    }

    @Test
    void softDeleteSharedDepenseMarksExpenseAndLinkedPaiementsAsDeleted() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        LogementRepository logementRepository = mock(LogementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository,
                logementRepository
        );

        Residence residence = new Residence();
        residence.setId(7L);

        Depense depense = new Depense();
        depense.setId(99L);
        depense.setResidence(residence);
        depense.setTypeDepense(TypeDepense.PARTAGE);

        com.resiflow.entity.Paiement paiement = new com.resiflow.entity.Paiement();
        paiement.setId(501L);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);

        when(depenseRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.of(depense));
        when(paiementRepository.findAllByDepense_Id(99L)).thenReturn(List.of(paiement));

        depenseService.softDeleteSharedDepense(99L, authenticatedUser);

        assertThat(depense.isDeleted()).isTrue();
        assertThat(paiement.isDeleted()).isTrue();
        verify(residenceAccessService).ensureAdminAccessToResidence(authenticatedUser, 7L);
        verify(depenseRepository).save(depense);
    }
}
