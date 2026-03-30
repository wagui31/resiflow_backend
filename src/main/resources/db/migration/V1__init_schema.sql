CREATE TABLE residences (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    address VARCHAR(255),
    created_at TIMESTAMP,
    enabled BOOLEAN
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    residence_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE invitations (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    target_value VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_invitations_token UNIQUE (token)
);
