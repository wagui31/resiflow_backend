ALTER TABLE depenses
    ADD COLUMN type_depense VARCHAR(20);

ALTER TABLE depenses
    ADD COLUMN montant_par_personne NUMERIC(12, 2);

UPDATE depenses
SET type_depense = 'CAGNOTTE'
WHERE type_depense IS NULL;

ALTER TABLE depenses
    ALTER COLUMN type_depense SET NOT NULL;

ALTER TABLE depenses
    ADD CONSTRAINT chk_depenses_type_montant_par_personne
        CHECK (
            (type_depense = 'CAGNOTTE' AND montant_par_personne IS NULL)
            OR (type_depense = 'PARTAGE' AND montant_par_personne IS NOT NULL AND montant_par_personne > 0)
        );

ALTER TABLE paiements
    ADD COLUMN depense_id BIGINT;

ALTER TABLE paiements
    ADD COLUMN type_paiement VARCHAR(30);

UPDATE paiements
SET type_paiement = 'CAGNOTTE'
WHERE type_paiement IS NULL;

ALTER TABLE paiements
    ALTER COLUMN type_paiement SET NOT NULL;

ALTER TABLE paiements
    ADD CONSTRAINT fk_paiements_depense
        FOREIGN KEY (depense_id) REFERENCES depenses (id);

ALTER TABLE paiements
    ADD CONSTRAINT chk_paiements_type_depense
        CHECK (
            (type_paiement = 'CAGNOTTE' AND depense_id IS NULL)
            OR (type_paiement = 'DEPENSE_PARTAGE' AND depense_id IS NOT NULL)
        );

CREATE INDEX idx_depenses_type_depense ON depenses (type_depense);
CREATE INDEX idx_paiements_depense_id ON paiements (depense_id);
CREATE INDEX idx_paiements_type_paiement ON paiements (type_paiement);
CREATE INDEX idx_paiements_depense_user_status ON paiements (depense_id, utilisateur_id, status);
