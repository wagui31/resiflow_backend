package com.resiflow.service;

import com.resiflow.dto.CreateDepenseRequest;
import com.resiflow.entity.Depense;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeDepense;
import com.resiflow.entity.User;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.DepenseRepository;
import com.resiflow.repository.PaiementRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.math.BigDecimal;
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
    void createDepenseRequiresCategorieIdForCagnotte() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository
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
    void createDepenseAllowsNullCategorieIdForPartage() {
        DepenseRepository depenseRepository = mock(DepenseRepository.class);
        CategorieDepenseService categorieDepenseService = mock(CategorieDepenseService.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);
        TransactionCagnotteService transactionCagnotteService = mock(TransactionCagnotteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);

        DepenseService depenseService = new DepenseService(
                depenseRepository,
                categorieDepenseService,
                residenceAccessService,
                transactionCagnotteService,
                userRepository,
                paiementRepository
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

        Depense savedDepense = new Depense();
        savedDepense.setResidence(residence);
        savedDepense.setCreePar(actor);
        savedDepense.setTypeDepense(TypeDepense.PARTAGE);
        savedDepense.setMontant(new BigDecimal("300.00"));
        savedDepense.setMontantParPersonne(new BigDecimal("100.00"));
        savedDepense.setDescription("Frais partages");

        when(residenceAccessService.getResidenceForAdmin(eq(7L), any(AuthenticatedUser.class))).thenReturn(residence);
        when(residenceAccessService.getRequiredActor(any())).thenReturn(actor);
        when(depenseRepository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(2L, "admin@example.com", 7L, UserRole.ADMIN);
        Depense result = depenseService.createDepense(request, authenticatedUser);

        assertThat(result.getCategorie()).isNull();
        assertThat(result.getTypeDepense()).isEqualTo(TypeDepense.PARTAGE);
        assertThat(result.getMontantParPersonne()).isEqualByComparingTo("100.00");
        verify(categorieDepenseService, never()).getRequiredCategorieDepense(any());
    }
}
