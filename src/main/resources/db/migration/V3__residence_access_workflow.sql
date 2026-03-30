ALTER TABLE residences
    ADD COLUMN code VARCHAR(255);

UPDATE residences
SET code = 'RES-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT || id::TEXT) FROM 1 FOR 6))
WHERE code IS NULL;

ALTER TABLE residences
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN address SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN enabled SET NOT NULL,
    ALTER COLUMN code SET NOT NULL;

ALTER TABLE residences
    ADD CONSTRAINT uk_residences_code UNIQUE (code);

ALTER TABLE users
    ADD COLUMN status VARCHAR(32);

UPDATE users
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE users
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

DROP TABLE IF EXISTS invitations;
