ALTER TABLE users
    ALTER COLUMN residence_id DROP NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_residence
        FOREIGN KEY (residence_id) REFERENCES residences (id);

CREATE INDEX idx_users_residence_id ON users (residence_id);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_invitations_residence_id ON invitations (residence_id);
