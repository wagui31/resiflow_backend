package com.resiflow.repository;

import com.resiflow.entity.PasswordResetRequest;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetRequest> findFirstByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetRequest> findFirstByResetSessionHashAndUsedAtIsNullAndInvalidatedAtIsNull(String resetSessionHash);

    long deleteByCreatedAtBefore(LocalDateTime cutoff);
}
