package com.resiflow.service;

import com.resiflow.dto.CreateLogementRequest;
import com.resiflow.dto.CreateLogementsBulkRequest;
import com.resiflow.dto.CreateLogementsBulkResponse;
import com.resiflow.dto.UpdateLogementRequest;
import com.resiflow.entity.Logement;
import com.resiflow.entity.Residence;
import com.resiflow.entity.TypeLogement;
import com.resiflow.entity.UserRole;
import com.resiflow.repository.LogementRepository;
import com.resiflow.security.AuthenticatedUser;
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
}
