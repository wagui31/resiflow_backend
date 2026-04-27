package com.resiflow.service;

import com.resiflow.dto.CreateLogementRequest;
import com.resiflow.dto.CreateLogementsBulkRequest;
import com.resiflow.dto.CreateLogementsBulkResponse;
import com.resiflow.dto.LogementOccupancyResponse;
import com.resiflow.dto.LogementResponse;
import com.resiflow.dto.PublicRegistrationCompositionType;
import com.resiflow.dto.PublicRegistrationContextResponse;
import com.resiflow.dto.PublicRegistrationFilterField;
import com.resiflow.dto.PublicRegistrationLogementResponse;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogementService {

    private final LogementRepository logementRepository;
    private final ResidenceService residenceService;
    private final ResidenceAccessService residenceAccessService;
    private final UserRepository userRepository;

    public LogementService(
            final LogementRepository logementRepository,
            final ResidenceService residenceService,
            final ResidenceAccessService residenceAccessService,
            final UserRepository userRepository
    ) {
        this.logementRepository = logementRepository;
        this.residenceService = residenceService;
        this.residenceAccessService = residenceAccessService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Logement createLogement(final CreateLogementRequest request, final AuthenticatedUser authenticatedUser) {
        validateCreateRequest(request);
        Logement logement = new Logement();
        logement.setResidence(residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser));
        logement.setTypeLogement(request.getTypeLogement());
        logement.setNumero(request.getNumero().trim());
        logement.setImmeuble(normalizeOptionalValue(request.getImmeuble()));
        logement.setEtage(normalizeOptionalValue(request.getEtage()));
        logement.setCodePostal(normalizeOptionalValue(request.getCodePostal()));
        logement.setAdresse(normalizeOptionalValue(request.getAdresse()));
        logement.setCodeInterne(buildCodeInterne(
                logement.getResidenceId(),
                logement.getTypeLogement(),
                logement.getImmeuble(),
                logement.getNumero()
        ));
        logement.setActive(Boolean.FALSE);
        logement.setDateActivation(null);
        return logementRepository.save(logement);
    }

    @Transactional
    public Logement updateLogement(
            final Long logementId,
            final UpdateLogementRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateUpdateRequest(request);
        Logement logement = getRequiredLogement(logementId);
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, logement.getResidenceId());
        logement.setTypeLogement(request.getTypeLogement());
        logement.setNumero(request.getNumero().trim());
        logement.setImmeuble(normalizeOptionalValue(request.getImmeuble()));
        logement.setEtage(normalizeOptionalValue(request.getEtage()));
        logement.setCodePostal(normalizeOptionalValue(request.getCodePostal()));
        logement.setAdresse(normalizeOptionalValue(request.getAdresse()));
        logement.setCodeInterne(buildCodeInterne(
                logement.getResidenceId(),
                logement.getTypeLogement(),
                logement.getImmeuble(),
                logement.getNumero()
        ));
        return logementRepository.save(logement);
    }

    @Transactional
    public CreateLogementsBulkResponse createLogementsBulk(
            final CreateLogementsBulkRequest request,
            final AuthenticatedUser authenticatedUser
    ) {
        validateBulkCreateRequest(request);
        Residence residence = residenceAccessService.getResidenceForAdmin(request.getResidenceId(), authenticatedUser);
        BulkNumeroRange range = parseBulkNumeroRange(request.getNumeroDebut(), request.getNumeroFin());

        List<Logement> logementsToCreate = new ArrayList<>();
        for (int numero = range.start(); numero <= range.end(); numero++) {
            String formattedNumero = formatNumero(numero, range.width());
            logementsToCreate.add(buildLogement(
                    residence,
                    request.getTypeLogement(),
                    formattedNumero,
                    request.getImmeuble(),
                    request.getEtage(),
                    request.getCodePostal(),
                    request.getAdresse()
            ));
        }

        for (Logement logement : logementsToCreate) {
            if (logementRepository.findByCodeInterne(logement.getCodeInterne()).isPresent()) {
                throw new IllegalArgumentException("Logement already exists for code interne " + logement.getCodeInterne());
            }
        }

        List<LogementResponse> responses = logementsToCreate.stream()
                .map(logementRepository::save)
                .map(LogementResponse::fromEntity)
                .toList();
        return new CreateLogementsBulkResponse(responses.size(), responses);
    }

    @Transactional(readOnly = true)
    public List<Logement> getLogementsByResidence(final Long residenceId, final AuthenticatedUser authenticatedUser) {
        residenceAccessService.getResidenceForMember(residenceId, authenticatedUser);
        return logementRepository.findAllByResidence_IdOrderByNumeroAsc(residenceId);
    }

    @Transactional(readOnly = true)
    public Logement getLogement(final Long logementId, final AuthenticatedUser authenticatedUser) {
        Logement logement = getRequiredLogement(logementId);
        residenceAccessService.ensureMemberAccessToResidence(authenticatedUser, logement.getResidenceId());
        return logement;
    }

    @Transactional
    public Logement activateLogement(final Long logementId, final AuthenticatedUser authenticatedUser) {
        Logement logement = getRequiredLogement(logementId);
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, logement.getResidenceId());
        if (!Boolean.TRUE.equals(logement.getActive())) {
            logement.setActive(Boolean.TRUE);
            logement.setDateActivation(LocalDateTime.now());
        }
        return logementRepository.save(logement);
    }

    @Transactional
    public Logement deactivateLogement(final Long logementId, final AuthenticatedUser authenticatedUser) {
        Logement logement = getRequiredLogement(logementId);
        residenceAccessService.ensureAdminAccessToResidence(authenticatedUser, logement.getResidenceId());
        logement.setActive(Boolean.FALSE);
        logement.setDateActivation(null);
        return logementRepository.save(logement);
    }

    @Transactional
    public Logement activateLogementAfterUserApproval(final Logement logement) {
        if (logement == null) {
            throw new IllegalArgumentException("Logement must not be null");
        }
        if (Boolean.TRUE.equals(logement.getActive())) {
            return logement;
        }
        logement.setActive(Boolean.TRUE);
        logement.setDateActivation(LocalDateTime.now());
        return logementRepository.save(logement);
    }

    @Transactional(readOnly = true)
    public LogementOccupancyResponse getOccupancy(final Long logementId, final AuthenticatedUser authenticatedUser) {
        Logement logement = getLogement(logementId, authenticatedUser);
        long occupiedCount = countActiveOccupants(logementId);
        int maxOccupants = logement.getResidence().getMaxOccupantsParLogement();
        return new LogementOccupancyResponse(logement.getId(), occupiedCount, maxOccupants, occupiedCount >= maxOccupants);
    }

    @Transactional(readOnly = true)
    public Logement getRequiredLogement(final Long logementId) {
        if (logementId == null) {
            throw new IllegalArgumentException("Logement ID must not be null");
        }
        return logementRepository.findById(logementId)
                .orElseThrow(() -> new NoSuchElementException("Logement not found: " + logementId));
    }

    @Transactional(readOnly = true)
    public List<PublicRegistrationLogementResponse> getPublicRegistrationLogements(final String residenceCode) {
        Residence residence = residenceService.getRequiredResidenceByCode(normalizeResidenceCode(residenceCode));
        int maxOccupants = residence.getMaxOccupantsParLogement();
        return sortForPublicRegistration(logementRepository.findAllByResidence_IdOrderByNumeroAsc(residence.getId())).stream()
                .map(logement -> PublicRegistrationLogementResponse.fromEntity(
                        logement,
                        countActiveOccupants(logement.getId()),
                        maxOccupants
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicRegistrationContextResponse getPublicRegistrationContext(final String residenceCode) {
        Residence residence = residenceService.getRequiredResidenceByCode(normalizeResidenceCode(residenceCode));
        List<Logement> logements = logementRepository.findAllByResidence_IdOrderByNumeroAsc(residence.getId());
        PublicRegistrationCompositionType compositionType = resolveCompositionType(logements);
        return new PublicRegistrationContextResponse(
                residence.getId(),
                residence.getCode(),
                compositionType,
                resolveAllowedFilters(compositionType),
                containsType(logements, TypeLogement.MAISON),
                containsType(logements, TypeLogement.APPARTEMENT),
                logements.size()
        );
    }

    @Transactional(readOnly = true)
    public PublicRegistrationSearchResponse searchPublicRegistrationLogements(
            final String residenceCode,
            final String numero,
            final String immeuble
    ) {
        Residence residence = residenceService.getRequiredResidenceByCode(normalizeResidenceCode(residenceCode));
        List<Logement> logements = logementRepository.findAllByResidence_IdOrderByNumeroAsc(residence.getId());
        PublicRegistrationCompositionType compositionType = resolveCompositionType(logements);
        String normalizedNumero = normalizeOptionalValue(numero);
        String normalizedImmeuble = normalizeOptionalValue(immeuble);
        List<PublicRegistrationFilterField> allowedFilters = resolveAllowedFilters(compositionType);

        if (!allowedFilters.contains(PublicRegistrationFilterField.IMMEUBLE)) {
            normalizedImmeuble = null;
        }
        final String numeroFilter = normalizedNumero;
        final String immeubleFilter = normalizedImmeuble;

        Predicate<Logement> filter = logement -> true;
        if (numeroFilter != null) {
            filter = filter.and(logement -> containsIgnoreCase(logement.getNumero(), numeroFilter));
        }
        if (immeubleFilter != null) {
            filter = filter.and(logement -> containsIgnoreCase(logement.getImmeuble(), immeubleFilter));
        }

        int maxOccupants = residence.getMaxOccupantsParLogement();
        List<PublicRegistrationLogementResponse> items = sortForPublicRegistration(logements).stream()
                .filter(filter)
                .map(logement -> PublicRegistrationLogementResponse.fromEntity(
                        logement,
                        countActiveOccupants(logement.getId()),
                        maxOccupants
                ))
                .toList();

        return new PublicRegistrationSearchResponse(
                residence.getId(),
                residence.getCode(),
                compositionType,
                allowedFilters,
                numeroFilter,
                immeubleFilter,
                items.size(),
                items
        );
    }

    @Transactional(readOnly = true)
    public long countActiveOccupants(final Long logementId) {
        return userRepository.countByLogement_IdAndStatusAndRoleIn(
                logementId,
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
    }

    @Transactional(readOnly = true)
    public void ensureLogementCanAcceptRegistration(final Long logementId) {
        Logement logement = getRequiredLogement(logementId);
        long occupiedCount = userRepository.countByLogement_IdAndStatusAndRoleIn(
                logementId,
                UserStatus.ACTIVE,
                List.of(UserRole.ADMIN, UserRole.USER)
        );
        if (occupiedCount >= logement.getResidence().getMaxOccupantsParLogement()) {
            throw new IllegalStateException("Logement has reached its maximum occupancy");
        }
    }

    @Transactional(readOnly = true)
    public void ensureLogementBelongsToResidence(final Long logementId, final Long residenceId) {
        Logement logement = getRequiredLogement(logementId);
        if (!logement.getResidenceId().equals(residenceId)) {
            throw new IllegalArgumentException("Logement does not belong to the selected residence");
        }
    }

    private void validateCreateRequest(final CreateLogementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create logement request must not be null");
        }
        if (request.getResidenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        validateCommonFields(request.getTypeLogement(), request.getNumero(), request.getImmeuble());
    }

    private void validateBulkCreateRequest(final CreateLogementsBulkRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create bulk logements request must not be null");
        }
        if (request.getResidenceId() == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        if (request.getNumeroDebut() == null || request.getNumeroDebut().trim().isEmpty()) {
            throw new IllegalArgumentException("Numero debut must not be blank");
        }
        if (request.getNumeroFin() == null || request.getNumeroFin().trim().isEmpty()) {
            throw new IllegalArgumentException("Numero fin must not be blank");
        }
        validateCommonFields(request.getTypeLogement(), request.getNumeroDebut(), request.getImmeuble());
    }

    private void validateUpdateRequest(final UpdateLogementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update logement request must not be null");
        }
        validateCommonFields(request.getTypeLogement(), request.getNumero(), request.getImmeuble());
    }

    private void validateCommonFields(final TypeLogement typeLogement, final String numero, final String immeuble) {
        if (typeLogement == null) {
            throw new IllegalArgumentException("Type logement must not be null");
        }
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("Numero must not be blank");
        }
        if (typeLogement == TypeLogement.APPARTEMENT && (immeuble == null || immeuble.trim().isEmpty())) {
            throw new IllegalArgumentException("Immeuble must not be blank for appartement");
        }
    }

    private String normalizeOptionalValue(final String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String normalizeResidenceCode(final String residenceCode) {
        if (residenceCode == null || residenceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Residence code must not be blank");
        }
        return residenceCode.trim().toUpperCase();
    }

    private List<Logement> sortForPublicRegistration(final List<Logement> logements) {
        return logements.stream()
                .sorted(Comparator
                        .comparing(Logement::getTypeLogement, Comparator.nullsLast(Enum::compareTo))
                        .thenComparing(logement -> normalizeForComparison(logement.getImmeuble()), Comparator.nullsLast(String::compareTo))
                        .thenComparing(logement -> normalizeForComparison(logement.getNumero()), Comparator.nullsLast(String::compareTo))
                        .thenComparing(Logement::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private PublicRegistrationCompositionType resolveCompositionType(final List<Logement> logements) {
        boolean hasMaison = containsType(logements, TypeLogement.MAISON);
        boolean hasAppartement = containsType(logements, TypeLogement.APPARTEMENT);
        if (!hasMaison && !hasAppartement) {
            return PublicRegistrationCompositionType.EMPTY;
        }
        if (hasMaison && hasAppartement) {
            return PublicRegistrationCompositionType.MIXED;
        }
        if (hasMaison) {
            return PublicRegistrationCompositionType.MAISON_ONLY;
        }
        return PublicRegistrationCompositionType.APPARTEMENT_ONLY;
    }

    private boolean containsType(final List<Logement> logements, final TypeLogement typeLogement) {
        return logements.stream().anyMatch(logement -> logement.getTypeLogement() == typeLogement);
    }

    private List<PublicRegistrationFilterField> resolveAllowedFilters(
            final PublicRegistrationCompositionType compositionType
    ) {
        return switch (compositionType) {
            case MAISON_ONLY -> List.of(PublicRegistrationFilterField.NUMERO);
            case APPARTEMENT_ONLY, MIXED -> List.of(
                    PublicRegistrationFilterField.IMMEUBLE,
                    PublicRegistrationFilterField.NUMERO
            );
            case EMPTY -> List.of();
        };
    }

    private boolean containsIgnoreCase(final String source, final String expectedFragment) {
        if (source == null || expectedFragment == null) {
            return false;
        }
        return normalizeForComparison(source).contains(normalizeForComparison(expectedFragment));
    }

    private String normalizeForComparison(final String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private Logement buildLogement(
            final Residence residence,
            final TypeLogement typeLogement,
            final String numero,
            final String immeuble,
            final String etage,
            final String codePostal,
            final String adresse
    ) {
        Logement logement = new Logement();
        logement.setResidence(residence);
        logement.setTypeLogement(typeLogement);
        logement.setNumero(numero.trim());
        logement.setImmeuble(normalizeOptionalValue(immeuble));
        logement.setEtage(normalizeOptionalValue(etage));
        logement.setCodePostal(normalizeOptionalValue(codePostal));
        logement.setAdresse(normalizeOptionalValue(adresse));
        logement.setCodeInterne(buildCodeInterne(
                logement.getResidenceId(),
                logement.getTypeLogement(),
                logement.getImmeuble(),
                logement.getNumero()
        ));
        logement.setActive(Boolean.FALSE);
        logement.setDateActivation(null);
        return logement;
    }

    private BulkNumeroRange parseBulkNumeroRange(final String numeroDebut, final String numeroFin) {
        String normalizedNumeroDebut = numeroDebut.trim();
        String normalizedNumeroFin = numeroFin.trim();
        if (!normalizedNumeroDebut.matches("\\d+")) {
            throw new IllegalArgumentException("Numero debut must be numeric");
        }
        if (!normalizedNumeroFin.matches("\\d+")) {
            throw new IllegalArgumentException("Numero fin must be numeric");
        }
        if (normalizedNumeroFin.length() > normalizedNumeroDebut.length()) {
            throw new IllegalArgumentException("Numero fin must not be longer than numero debut");
        }

        int start = Integer.parseInt(normalizedNumeroDebut);
        int end = Integer.parseInt(normalizedNumeroFin);
        if (start > end) {
            throw new IllegalArgumentException("Numero debut must be less than or equal to numero fin");
        }
        return new BulkNumeroRange(start, end, normalizedNumeroDebut.length());
    }

    private String formatNumero(final int numero, final int width) {
        return String.format("%0" + width + "d", numero);
    }

    private String buildCodeInterne(
            final Long residenceId,
            final TypeLogement typeLogement,
            final String immeuble,
            final String numero
    ) {
        if (residenceId == null) {
            throw new IllegalArgumentException("Residence ID must not be null");
        }
        String normalizedNumero = normalizeCodeSegment(numero);
        String residencePrefix = "RES" + residenceId + "-";
        if (typeLogement == TypeLogement.MAISON) {
            return residencePrefix + TypeLogement.MAISON.name() + "-" + normalizedNumero;
        }
        return residencePrefix
                + TypeLogement.APPARTEMENT.name()
                + "-"
                + normalizeCodeSegment(immeuble)
                + "-"
                + normalizedNumero;
    }

    private String normalizeCodeSegment(final String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private record BulkNumeroRange(int start, int end, int width) {
    }
}
