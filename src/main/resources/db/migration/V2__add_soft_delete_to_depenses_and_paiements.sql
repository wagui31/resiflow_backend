SET search_path TO resiflow;

ALTER TABLE depenses
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE paiements
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_depenses_residence_deleted ON depenses (residence_id, is_deleted);
CREATE INDEX idx_depenses_type_statut_deleted ON depenses (type_depense, statut, is_deleted);

CREATE INDEX idx_paiements_residence_deleted ON paiements (residence_id, is_deleted);
CREATE INDEX idx_paiements_logement_status_deleted ON paiements (logement_id, status, is_deleted);
CREATE INDEX idx_paiements_depense_deleted ON paiements (depense_id, is_deleted);
