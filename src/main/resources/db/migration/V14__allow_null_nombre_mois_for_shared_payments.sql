ALTER TABLE paiements
    ALTER COLUMN nombre_mois DROP NOT NULL;

ALTER TABLE paiements
    DROP CONSTRAINT chk_paiements_nombre_mois;

ALTER TABLE paiements
    ADD CONSTRAINT chk_paiements_nombre_mois
        CHECK (
            (type_paiement = 'CAGNOTTE' AND nombre_mois IS NOT NULL AND nombre_mois > 0)
            OR (type_paiement = 'DEPENSE_PARTAGE' AND nombre_mois IS NULL)
        );
