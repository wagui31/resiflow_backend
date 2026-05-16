package com.resiflow.service;

import com.resiflow.dto.UpdateResidenceAdminSettingsRequest;
import com.resiflow.entity.Residence;
import com.resiflow.repository.ResidenceRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResidenceServiceTest {

    @Test
    void updateResidenceAdminSettingsUpdatesEditableFields() {
        AtomicReference<Residence> savedResidenceRef = new AtomicReference<>();
        Residence existingResidence = new Residence();
        existingResidence.setId(7L);
        existingResidence.setName("Residence Initiale");
        existingResidence.setAddress("1 rue Initiale");
        existingResidence.setCode("RES-OLD");
        existingResidence.setMontantMensuel(new BigDecimal("80.00"));
        existingResidence.setCurrency("EUR");
        existingResidence.setMaxOccupantsParLogement(3);
        existingResidence.setEnabled(Boolean.TRUE);
        existingResidence.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        existingResidence.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        ResidenceService service = new ResidenceService(repositoryProxy(existingResidence, savedResidenceRef));

        UpdateResidenceAdminSettingsRequest request = new UpdateResidenceAdminSettingsRequest();
        request.setName("Residence Horizon");
        request.setAddress("12 rue des Fleurs");
        request.setCode("RES-HZN");
        request.setMontantMensuel(new BigDecimal("95.00"));
        request.setMaxOccupantsParLogement(5);

        Residence updated = service.updateResidenceAdminSettings(7L, request);

        assertThat(updated.getName()).isEqualTo("Residence Horizon");
        assertThat(updated.getAddress()).isEqualTo("12 rue des Fleurs");
        assertThat(updated.getCode()).isEqualTo("RES-HZN");
        assertThat(updated.getMontantMensuel()).isEqualByComparingTo("95.00");
        assertThat(updated.getCurrency()).isEqualTo("EUR");
        assertThat(updated.getMaxOccupantsParLogement()).isEqualTo(5);
        assertThat(savedResidenceRef.get()).isNotNull();
    }

    @Test
    void updateResidenceAdminSettingsRejectsMaxOccupantsGreaterThanFive() {
        Residence existingResidence = new Residence();
        existingResidence.setId(7L);
        existingResidence.setCode("RES-OLD");

        ResidenceService service = new ResidenceService(repositoryProxy(existingResidence, new AtomicReference<>()));

        UpdateResidenceAdminSettingsRequest request = new UpdateResidenceAdminSettingsRequest();
        request.setName("Residence Horizon");
        request.setAddress("12 rue des Fleurs");
        request.setCode("RES-HZN");
        request.setMontantMensuel(new BigDecimal("95.00"));
        request.setMaxOccupantsParLogement(6);

        assertThatThrownBy(() -> service.updateResidenceAdminSettings(7L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Residence max occupants per logement must be between 1 and 5");
    }

    private ResidenceRepository repositoryProxy(
            final Residence existingResidence,
            final AtomicReference<Residence> savedResidenceRef
    ) {
        return (ResidenceRepository) Proxy.newProxyInstance(
                ResidenceRepository.class.getClassLoader(),
                new Class<?>[]{ResidenceRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(existingResidence);
                    }
                    if ("findByCode".equals(method.getName())) {
                        final String code = (String) args[0];
                        if (existingResidence.getCode() != null && existingResidence.getCode().equals(code)) {
                            return Optional.of(existingResidence);
                        }
                        return Optional.empty();
                    }
                    if ("existsByCode".equals(method.getName())) {
                        final String code = (String) args[0];
                        return existingResidence.getCode() != null && existingResidence.getCode().equals(code);
                    }
                    if ("save".equals(method.getName())) {
                        Residence saved = (Residence) args[0];
                        savedResidenceRef.set(saved);
                        return saved;
                    }
                    if ("toString".equals(method.getName())) {
                        return "ResidenceRepositoryTestProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                }
        );
    }
}
