package com.resiflow.service;

import com.resiflow.config.PasswordResetProperties;
import com.resiflow.repository.PasswordResetRequestRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetRequestCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetRequestCleanupService.class);

    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordResetProperties passwordResetProperties;

    public PasswordResetRequestCleanupService(
            final PasswordResetRequestRepository passwordResetRequestRepository,
            final PasswordResetProperties passwordResetProperties
    ) {
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.passwordResetProperties = passwordResetProperties;
    }

    @Scheduled(cron = "${app.auth.password-reset.cleanup-cron:0 0 * * * *}")
    @Transactional
    public void cleanupOldRequests() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(passwordResetProperties.cleanupRetentionDays());
        long deletedCount = passwordResetRequestRepository.deleteByCreatedAtBefore(cutoff);
        if (deletedCount > 0) {
            LOGGER.info("Password reset cleanup deleted {} request(s) older than {}", deletedCount, cutoff);
        }
    }
}
