package com.resiflow.service;

import com.resiflow.dto.CreateLogementRequest;
import com.resiflow.dto.CreateLogementsBulkRequest;
import com.resiflow.dto.CreateLogementsBulkResponse;
import com.resiflow.dto.PublicRegistrationCompositionType;
import com.resiflow.dto.PublicRegistrationContextResponse;
import com.resiflow.dto.PublicRegistrationSearchResponse;
import com.resiflow.dto.UpdateLogementRequest;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.UserRole;
import com.resiflow.entity.UserStatus;
import com.resiflow.repository.LogementRepository;
import com.resiflow.repository.UserRepository;
import com.resiflow.security.AuthenticatedUser;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogementServiceTest {

    @Test
    void createLogementBuildsCodeInterneForMaison() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        Residence residence = new Residence();
        residence.setId(7L);
        when(residenceAccessService.getResidenceForAdmin(any(), any())).thenReturn(residence);
        when(logementRepository.save(any(Logement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateLogementRequest request = new CreateLogementRequest();
        request.setResidenceId(7L);
        request.setTypeLogement(TypeLogement.MAISON);
        request.setNumero("01");

        Logement result = logementService.createLogement(
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        );

        assertThat(result.getCodeInterne()).isEqualTo("RES7-MAISON-01");
    }

    @Test
    void createLogementBuildsCodeInterneForAppartement() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        Residence residence = new Residence();
        residence.setId(7L);
        when(residenceAccessService.getResidenceForAdmin(any(), any())).thenReturn(residence);
        when(logementRepository.save(any(Logement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateLogementRequest request = new CreateLogementRequest();
        request.setResidenceId(7L);
        request.setTypeLogement(TypeLogement.APPARTEMENT);
        request.setImmeuble("H1");
        request.setNumero("01");

        Logement result = logementService.createLogement(
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        );

        assertThat(result.getCodeInterne()).isEqualTo("RES7-APPARTEMENT-H1-01");
    }

    @Test
    void updateLogementRecomputesCodeInterne() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        Residence residence = new Residence();
        residence.setId(7L);
        Logement logement = new Logement();
        logement.setId(10L);
        logement.setResidence(residence);
        logement.setTypeLogement(TypeLogement.MAISON);
        logement.setNumero("01");
        logement.setCodeInterne("RES7-MAISON-01");

        when(logementRepository.findById(10L)).thenReturn(Optional.of(logement));
        when(logementRepository.save(any(Logement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateLogementRequest request = new UpdateLogementRequest();
        request.setTypeLogement(TypeLogement.APPARTEMENT);
        request.setImmeuble("H1");
        request.setNumero("02");

        Logement result = logementService.updateLogement(
                10L,
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        );

        assertThat(result.getCodeInterne()).isEqualTo("RES7-APPARTEMENT-H1-02");
    }

    @Test
    void createLogementRejectsAppartementWithoutImmeuble() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        CreateLogementRequest request = new CreateLogementRequest();
        request.setResidenceId(7L);
        request.setTypeLogement(TypeLogement.APPARTEMENT);
        request.setNumero("01");

        assertThatThrownBy(() -> logementService.createLogement(
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Immeuble must not be blank for appartement");
    }

    @Test
    void createLogementsBulkCreatesInclusiveRangeWithPadding() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        Residence residence = new Residence();
        residence.setId(7L);
        when(residenceAccessService.getResidenceForAdmin(any(), any())).thenReturn(residence);
        when(logementRepository.findByCodeInterne(any())).thenReturn(Optional.empty());
        when(logementRepository.save(any(Logement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateLogementsBulkRequest request = new CreateLogementsBulkRequest();
        request.setResidenceId(7L);
        request.setTypeLogement(TypeLogement.MAISON);
        request.setNumeroDebut("001");
        request.setNumeroFin("003");

        CreateLogementsBulkResponse result = logementService.createLogementsBulk(
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        );

        assertThat(result.getCreatedCount()).isEqualTo(3);
        assertThat(result.getLogements()).extracting("numero").containsExactly("001", "002", "003");
        assertThat(result.getLogements()).extracting("codeInterne")
                .containsExactly("RES7-MAISON-001", "RES7-MAISON-002", "RES7-MAISON-003");
    }

    @Test
    void createLogementsBulkRejectsExistingCodeInterne() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        Residence residence = new Residence();
        residence.setId(7L);
        when(residenceAccessService.getResidenceForAdmin(any(), any())).thenReturn(residence);

        Logement existingLogement = new Logement();
        existingLogement.setCodeInterne("RES7-MAISON-002");
        when(logementRepository.findByCodeInterne("RES7-MAISON-001")).thenReturn(Optional.empty());
        when(logementRepository.findByCodeInterne("RES7-MAISON-002")).thenReturn(Optional.of(existingLogement));

        CreateLogementsBulkRequest request = new CreateLogementsBulkRequest();
        request.setResidenceId(7L);
        request.setTypeLogement(TypeLogement.MAISON);
        request.setNumeroDebut("001");
        request.setNumeroFin("002");

        assertThatThrownBy(() -> logementService.createLogementsBulk(
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Logement already exists for code interne RES7-MAISON-002");
    }

    @Test
    void createLogementsBulkRejectsNumeroFinLongerThanNumeroDebut() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        ResidenceAccessService residenceAccessService = mock(ResidenceAccessService.class);

        LogementService logementService = new LogementService(logementRepository, null, residenceAccessService, null);

        CreateLogementsBulkRequest request = new CreateLogementsBulkRequest();
        request.setResidenceId(7L);
        request.setTypeLogement(TypeLogement.MAISON);
        request.setNumeroDebut("01");
        request.setNumeroFin("100");

        assertThatThrownBy(() -> logementService.createLogementsBulk(
                request,
                new AuthenticatedUser(1L, "admin@example.com", 7L, UserRole.ADMIN)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Numero fin must not be longer than numero debut");
    }

    @Test
    void getPublicRegistrationContextReturnsMaisonOnlyComposition() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        com.resiflow.service.ResidenceService residenceService = mock(com.resiflow.service.ResidenceService.class);

        LogementService logementService = new LogementService(logementRepository, residenceService, null, null);

        Residence residence = new Residence();
        residence.setId(7L);
        residence.setCode("RES-ABC123");
        residence.setMaxOccupantsParLogement(3);

        Logement maison = new Logement();
        maison.setId(10L);
        maison.setResidence(residence);
        maison.setTypeLogement(TypeLogement.MAISON);
        maison.setNumero("001");

        when(residenceService.getRequiredResidenceByCode("RES-ABC123")).thenReturn(residence);
        when(logementRepository.findAllByResidence_IdOrderByNumeroAsc(7L)).thenReturn(List.of(maison));

        PublicRegistrationContextResponse result = logementService.getPublicRegistrationContext("RES-ABC123");

        assertThat(result.getCompositionType()).isEqualTo(PublicRegistrationCompositionType.MAISON_ONLY);
        assertThat(result.getAllowedFilters()).extracting(Enum::name).containsExactly("NUMERO");
        assertThat(result.isHasMaison()).isTrue();
        assertThat(result.isHasAppartement()).isFalse();
        assertThat(result.getTotalLogements()).isEqualTo(1);
    }

    @Test
    void searchPublicRegistrationLogementsAppliesAppartementFiltersOnBackend() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        com.resiflow.service.ResidenceService residenceService = mock(com.resiflow.service.ResidenceService.class);
        UserRepository userRepository = mock(UserRepository.class);

        LogementService logementService = new LogementService(logementRepository, residenceService, null, userRepository);

        Residence residence = new Residence();
        residence.setId(7L);
        residence.setCode("RES-ABC123");
        residence.setMaxOccupantsParLogement(3);

        Logement appartement101 = new Logement();
        appartement101.setId(10L);
        appartement101.setResidence(residence);
        appartement101.setTypeLogement(TypeLogement.APPARTEMENT);
        appartement101.setImmeuble("B");
        appartement101.setNumero("101");
        appartement101.setCodeInterne("RES7-APPARTEMENT-B-101");
        appartement101.setActive(Boolean.TRUE);

        Logement appartement102 = new Logement();
        appartement102.setId(11L);
        appartement102.setResidence(residence);
        appartement102.setTypeLogement(TypeLogement.APPARTEMENT);
        appartement102.setImmeuble("B");
        appartement102.setNumero("102");
        appartement102.setCodeInterne("RES7-APPARTEMENT-B-102");
        appartement102.setActive(Boolean.TRUE);

        when(residenceService.getRequiredResidenceByCode("RES-ABC123")).thenReturn(residence);
        when(logementRepository.findAllByResidence_IdOrderByNumeroAsc(7L)).thenReturn(List.of(appartement102, appartement101));
        when(userRepository.countByLogement_IdAndStatusAndRoleIn(10L, UserStatus.ACTIVE, List.of(UserRole.ADMIN, UserRole.USER)))
                .thenReturn(1L);
        when(userRepository.countByLogement_IdAndStatusAndRoleIn(11L, UserStatus.ACTIVE, List.of(UserRole.ADMIN, UserRole.USER)))
                .thenReturn(2L);

        PublicRegistrationSearchResponse result = logementService.searchPublicRegistrationLogements("RES-ABC123", "02", "b");

        assertThat(result.getCompositionType()).isEqualTo(PublicRegistrationCompositionType.APPARTEMENT_ONLY);
        assertThat(result.getNumeroFilter()).isEqualTo("02");
        assertThat(result.getImmeubleFilter()).isEqualTo("b");
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getLogementId()).isEqualTo(11L);
        assertThat(result.getItems().get(0).getCodeInterne()).isEqualTo("RES7-APPARTEMENT-B-102");
    }

    @Test
    void searchPublicRegistrationLogementsIgnoresImmeubleFilterForMaisonOnlyResidence() {
        LogementRepository logementRepository = mock(LogementRepository.class);
        com.resiflow.service.ResidenceService residenceService = mock(com.resiflow.service.ResidenceService.class);
        UserRepository userRepository = mock(UserRepository.class);

        LogementService logementService = new LogementService(logementRepository, residenceService, null, userRepository);

        Residence residence = new Residence();
        residence.setId(7L);
        residence.setCode("RES-ABC123");
        residence.setMaxOccupantsParLogement(3);

        Logement maison001 = new Logement();
        maison001.setId(10L);
        maison001.setResidence(residence);
        maison001.setTypeLogement(TypeLogement.MAISON);
        maison001.setNumero("0001");
        maison001.setCodeInterne("RES7-MAISON-0001");
        maison001.setActive(Boolean.FALSE);

        Logement maison002 = new Logement();
        maison002.setId(11L);
        maison002.setResidence(residence);
        maison002.setTypeLogement(TypeLogement.MAISON);
        maison002.setNumero("0200");
        maison002.setCodeInterne("RES7-MAISON-0200");
        maison002.setActive(Boolean.FALSE);

        when(residenceService.getRequiredResidenceByCode("RES-ABC123")).thenReturn(residence);
        when(logementRepository.findAllByResidence_IdOrderByNumeroAsc(7L)).thenReturn(List.of(maison002, maison001));
        when(userRepository.countByLogement_IdAndStatusAndRoleIn(10L, UserStatus.ACTIVE, List.of(UserRole.ADMIN, UserRole.USER)))
                .thenReturn(0L);
        when(userRepository.countByLogement_IdAndStatusAndRoleIn(11L, UserStatus.ACTIVE, List.of(UserRole.ADMIN, UserRole.USER)))
                .thenReturn(0L);

        PublicRegistrationSearchResponse result = logementService.searchPublicRegistrationLogements("RES-ABC123", "001", "A");

        assertThat(result.getCompositionType()).isEqualTo(PublicRegistrationCompositionType.MAISON_ONLY);
        assertThat(result.getImmeubleFilter()).isNull();
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getItems()).extracting("numero").containsExactly("0001");
    }
}
