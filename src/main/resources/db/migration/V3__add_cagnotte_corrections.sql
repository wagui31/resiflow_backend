SET search_path TO resiflow;

CREATE TABLE corrections_cagnotte (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    ancien_solde NUMERIC(12, 2) NOT NULL,
    nouveau_solde NUMERIC(12, 2) NOT NULL,
    delta NUMERIC(12, 2) NOT NULL,
    motif VARCHAR(500) NOT NULL,
    cree_par BIGINT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    CONSTRAINT fk_corrections_cagnotte_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_corrections_cagnotte_cree_par FOREIGN KEY (cree_par) REFERENCES users (id),
    CONSTRAINT chk_corrections_cagnotte_nouveau_solde CHECK (nouveau_solde >= 0),
    CONSTRAINT chk_corrections_cagnotte_delta CHECK (delta <> 0)
);

CREATE INDEX idx_corrections_cagnotte_residence_id ON corrections_cagnotte (residence_id);
CREATE INDEX idx_corrections_cagnotte_cree_par ON corrections_cagnotte (cree_par);

ALTER TABLE transactions_cagnotte
    ALTER COLUMN reference_id DROP NOT NULL;

ALTER TABLE transactions_cagnotte
    DROP CONSTRAINT chk_transactions_cagnotte_montant;

ALTER TABLE transactions_cagnotte
    ADD CONSTRAINT chk_transactions_cagnotte_montant_by_type CHECK (
        (type IN ('CONTRIBUTION', 'DEPENSE') AND montant > 0)
        OR (type = 'CORRECTION' AND montant <> 0)
    );
