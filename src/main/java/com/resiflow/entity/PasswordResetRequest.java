package com.resiflow.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_reset_requests")
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email_snapshot", nullable = false)
    private String emailSnapshot;

    @Column(name = "code_hash")
    private String codeHash;

    @Column(name = "reset_session_hash")
    private String resetSessionHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "reset_session_expires_at")
    private LocalDateTime resetSessionExpiresAt;

    @Column(name = "code_verified_at")
    private LocalDateTime codeVerifiedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "invalidated_at")
    private LocalDateTime invalidatedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "resend_count", nullable = false)
    private int resendCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public String getEmailSnapshot() {
        return emailSnapshot;
    }

    public void setEmailSnapshot(final String emailSnapshot) {
        this.emailSnapshot = emailSnapshot;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(final String codeHash) {
        this.codeHash = codeHash;
    }

    public String getResetSessionHash() {
        return resetSessionHash;
    }

    public void setResetSessionHash(final String resetSessionHash) {
        this.resetSessionHash = resetSessionHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getResetSessionExpiresAt() {
        return resetSessionExpiresAt;
    }

    public void setResetSessionExpiresAt(final LocalDateTime resetSessionExpiresAt) {
        this.resetSessionExpiresAt = resetSessionExpiresAt;
    }

    public LocalDateTime getCodeVerifiedAt() {
        return codeVerifiedAt;
    }

    public void setCodeVerifiedAt(final LocalDateTime codeVerifiedAt) {
        this.codeVerifiedAt = codeVerifiedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(final LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getInvalidatedAt() {
        return invalidatedAt;
    }

    public void setInvalidatedAt(final LocalDateTime invalidatedAt) {
        this.invalidatedAt = invalidatedAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(final int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getResendCount() {
        return resendCount;
    }

    public void setResendCount(final int resendCount) {
        this.resendCount = resendCount;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(final LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(final LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (lastSentAt == null) {
            lastSentAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
