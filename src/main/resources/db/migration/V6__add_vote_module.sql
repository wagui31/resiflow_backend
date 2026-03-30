CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    titre VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    montant_estime NUMERIC(12, 2),
    statut VARCHAR(20) NOT NULL,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    cree_par BIGINT NOT NULL,
    depense_id BIGINT,
    CONSTRAINT fk_votes_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_votes_cree_par FOREIGN KEY (cree_par) REFERENCES users (id),
    CONSTRAINT fk_votes_depense FOREIGN KEY (depense_id) REFERENCES depenses (id),
    CONSTRAINT chk_votes_montant_estime CHECK (montant_estime IS NULL OR montant_estime > 0),
    CONSTRAINT chk_votes_dates CHECK (date_fin > date_debut)
);

CREATE TABLE vote_utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    vote_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    choix VARCHAR(20) NOT NULL,
    date_vote TIMESTAMP NOT NULL,
    CONSTRAINT fk_vote_utilisateurs_vote FOREIGN KEY (vote_id) REFERENCES votes (id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_utilisateurs_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES users (id),
    CONSTRAINT uk_vote_utilisateurs_vote_utilisateur UNIQUE (vote_id, utilisateur_id)
);

CREATE INDEX idx_votes_residence_id ON votes (residence_id);
CREATE INDEX idx_votes_statut_date_fin ON votes (statut, date_fin);
CREATE INDEX idx_vote_utilisateurs_vote_id ON vote_utilisateurs (vote_id);
CREATE INDEX idx_vote_utilisateurs_utilisateur_id ON vote_utilisateurs (utilisateur_id);
