ALTER TABLE residences
    ADD COLUMN montant_mensuel NUMERIC(12, 2);

UPDATE residences
SET montant_mensuel = 0.00
WHERE montant_mensuel IS NULL;

ALTER TABLE residences
    ALTER COLUMN montant_mensuel SET NOT NULL;

ALTER TABLE users
    ADD COLUMN statut_paiement VARCHAR(20) NOT NULL DEFAULT 'EN_RETARD';

CREATE TABLE paiements (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    residence_id BIGINT NOT NULL,
    nombre_mois INTEGER NOT NULL,
    montant_mensuel NUMERIC(12, 2) NOT NULL,
    montant_total NUMERIC(12, 2) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    date_paiement TIMESTAMP NOT NULL,
    cree_par BIGINT NOT NULL,
    CONSTRAINT fk_paiements_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES users (id),
    CONSTRAINT fk_paiements_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_paiements_cree_par FOREIGN KEY (cree_par) REFERENCES users (id),
    CONSTRAINT chk_paiements_nombre_mois CHECK (nombre_mois > 0),
    CONSTRAINT chk_paiements_montants CHECK (montant_mensuel > 0 AND montant_total > 0)
);

CREATE TABLE depenses (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    montant NUMERIC(12, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    statut VARCHAR(20) NOT NULL,
    cree_par BIGINT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    valide_par BIGINT,
    date_validation TIMESTAMP,
    CONSTRAINT fk_depenses_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_depenses_cree_par FOREIGN KEY (cree_par) REFERENCES users (id),
    CONSTRAINT fk_depenses_valide_par FOREIGN KEY (valide_par) REFERENCES users (id),
    CONSTRAINT chk_depenses_montant CHECK (montant > 0)
);

CREATE TABLE transactions_cagnotte (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    user_id BIGINT,
    type VARCHAR(20) NOT NULL,
    montant NUMERIC(12, 2) NOT NULL,
    reference_id BIGINT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    CONSTRAINT fk_transactions_cagnotte_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_transactions_cagnotte_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_transactions_cagnotte_montant CHECK (montant > 0)
);

CREATE INDEX idx_paiements_utilisateur_id ON paiements (utilisateur_id);
CREATE INDEX idx_paiements_residence_id ON paiements (residence_id);
CREATE INDEX idx_paiements_date_fin ON paiements (date_fin);
CREATE INDEX idx_depenses_residence_id ON depenses (residence_id);
CREATE INDEX idx_depenses_statut ON depenses (statut);
CREATE INDEX idx_transactions_cagnotte_residence_id ON transactions_cagnotte (residence_id);
CREATE INDEX idx_transactions_cagnotte_type ON transactions_cagnotte (type);
