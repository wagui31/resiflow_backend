CREATE TABLE user_push_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    installation_id VARCHAR(128) NOT NULL,
    device_name VARCHAR(255),
    app_version VARCHAR(80),
    status VARCHAR(32) NOT NULL,
    last_seen_at TIMESTAMP NOT NULL,
    invalidated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_push_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_user_push_tokens_token UNIQUE (token)
);

CREATE INDEX idx_user_push_tokens_user_status
    ON user_push_tokens (user_id, status);

CREATE INDEX idx_user_push_tokens_installation
    ON user_push_tokens (installation_id);

CREATE INDEX idx_user_push_tokens_status
    ON user_push_tokens (status);
