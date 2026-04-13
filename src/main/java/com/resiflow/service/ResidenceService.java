package com.resiflow.service;

import com.resiflow.dto.CreateResidenceRequest;
import com.resiflow.entity.Residence;
import com.resiflow.repository.ResidenceRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ResidenceService {

    private final ResidenceRepository residenceRepository;

    public ResidenceService(final ResidenceRepository residenceRepository) {
        this.residenceRepository = residenceRepository;
    }

    @Transactional
    public Residence createResidence(final CreateResidenceRequest request) {
        validateCreateRequest(request);

        Residence residence = new Residence();
        LocalDateTime now = LocalDateTime.now();
        residence.setName(request.getName().trim());
        residence.setAddress(request.getAddress().trim());
        residence.setCode(resolveResidenceCode(request.getCode(), null));
        residence.setEnabled(request.getEnabled() == null || request.getEnabled());
        residence.setMontantMensuel(request.getMontantMensuel());
        residence.setCurrency(normalizeCurrency(request.getCurrency()));
        residence.setMaxOccupantsParLogement(request.getMaxOccupantsParLogement());
        residence.setCreatedAt(now);
        residence.setUpdatedAt(now);

        return residenceRepository.save(residence);
    }

    public List<Residence> getAllResidences() {
        return residenceRepository.findAll();
    }

    @Transactional
    public Residence updateResidence(final Long residenceId, final CreateResidenceRequest request) {
        validateCreateRequest(request);

        Residence residence = getRequiredResidence(residenceId);
        residence.setName(request.getName().trim());
        residence.setAddress(request.getAddress().trim());
        if (!isBlank(request.getCode())) {
            residence.setCode(resolveResidenceCode(request.getCode(), residenceId));
        }
        residence.setEnabled(request.getEnabled() == null || request.getEnabled());
        residence.setMontantMensuel(request.getMontantMensuel());
        residence.setCurrency(normalizeCurrency(request.getCurrency()));
        residence.setMaxOccupantsParLogement(request.getMaxOccupantsParLogement());
        residence.setUpdatedAt(LocalDateTime.now());

        return residenceRepository.save(residence);
    }

    public void deleteResidence(final Long residenceId) {
        Residence residence = getRequiredResidence(residenceId);
        residenceRepository.delete(residence);
    }

    public Residence getRequiredResidence(final Long residenceId) {
        if (residenceId == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }

        return residenceRepository.findById(residenceId)
                .orElseThrow(() -> new NoSuchElementException("Residence not found: " + residenceId));
    }

    public Residence getRequiredResidenceByCode(final String residenceCode) {
        if (isBlank(residenceCode)) {
            throw new IllegalArgumentException("Residence code must not be blank");
        }

        return residenceRepository.findByCode(residenceCode.trim())
                .orElseThrow(() -> new InvalidResidenceCodeException("Invalid residence code"));
    }

    private void validateCreateRequest(final CreateResidenceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create residence request must not be null");
        }
        if (isBlank(request.getName())) {
            throw new IllegalArgumentException("Residence name must not be blank");
        }
        if (isBlank(request.getAddress())) {
            throw new IllegalArgumentException("Residence address must not be blank");
        }
        if (request.getMontantMensuel() == null) {
            throw new IllegalArgumentException("Residence monthly amount must not be null");
        }
        if (request.getMontantMensuel().signum() <= 0) {
            throw new IllegalArgumentException("Residence monthly amount must be greater than zero");
        }
        if (isBlank(request.getCurrency())) {
            throw new IllegalArgumentException("Residence currency must not be blank");
        }
        if (!isValidCurrency(request.getCurrency())) {
            throw new IllegalArgumentException("Residence currency must be a 3-letter ISO code");
        }
        if (request.getMaxOccupantsParLogement() == null || request.getMaxOccupantsParLogement() <= 0) {
            throw new IllegalArgumentException("Residence max occupants per logement must be greater than zero");
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeCurrency(final String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private boolean isValidCurrency(final String value) {
        String normalizedValue = normalizeCurrency(value);
        return normalizedValue != null && normalizedValue.matches("[A-Z]{3}");
    }

    private String resolveResidenceCode(final String requestedCode, final Long residenceId) {
        String candidate = isBlank(requestedCode) ? generateUniqueCode() : requestedCode.trim().toUpperCase();

        boolean alreadyUsed = residenceRepository.existsByCode(candidate);
        if (alreadyUsed && residenceId != null) {
            Residence existingResidence = residenceRepository.findByCode(candidate).orElse(null);
            if (existingResidence != null && residenceId.equals(existingResidence.getId())) {
                return candidate;
            }
        }

        if (alreadyUsed) {
            throw new IllegalArgumentException("Residence code is already used");
        }

        return candidate;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String candidate = "RES-" + randomAlphaNumeric(6);
            if (!residenceRepository.existsByCode(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to generate a unique residence code");
    }

    private String randomAlphaNumeric(final int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            int selectedIndex = ThreadLocalRandom.current().nextInt(characters.length());
            builder.append(characters.charAt(selectedIndex));
        }
        return builder.toString();
    }
}
