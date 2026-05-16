SET search_path TO resiflow;

CREATE TABLE password_reset_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email_snapshot VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255),
    reset_session_hash VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    reset_session_expires_at TIMESTAMP,
    code_verified_at TIMESTAMP,
    used_at TIMESTAMP,
    invalidated_at TIMESTAMP,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    resend_count INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    last_sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_requests_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_password_reset_requests_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT chk_password_reset_requests_resend_count CHECK (resend_count >= 0)
);

CREATE INDEX idx_password_reset_requests_user_id
    ON password_reset_requests (user_id);

CREATE INDEX idx_password_reset_requests_expires_at
    ON password_reset_requests (expires_at);

CREATE INDEX idx_password_reset_requests_open
    ON password_reset_requests (user_id, used_at, invalidated_at, created_at DESC);

CREATE INDEX idx_password_reset_requests_reset_session_hash
    ON password_reset_requests (reset_session_hash);
