CREATE SCHEMA IF NOT EXISTS resiflow;
SET search_path TO resiflow;

CREATE TABLE residences (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    montant_mensuel NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3),
    max_occupants_par_logement INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    enabled BOOLEAN NOT NULL,
    CONSTRAINT uk_residences_code UNIQUE (code)
);

CREATE TABLE logements (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    type_logement VARCHAR(20) NOT NULL,
    numero VARCHAR(255) NOT NULL,
    immeuble VARCHAR(255),
    etage VARCHAR(255),
    code_postal VARCHAR(20),
    adresse VARCHAR(255),
    code_interne VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    date_activation TIMESTAMP,
    CONSTRAINT fk_logements_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT uk_logements_code_interne UNIQUE (code_interne),
    CONSTRAINT chk_logements_type_logement CHECK (type_logement IN ('APPARTEMENT', 'MAISON'))
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    residence_id BIGINT,
    logement_id BIGINT,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    date_entree_residence DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_users_logement FOREIGN KEY (logement_id) REFERENCES logements (id)
);

CREATE TABLE categories_depenses (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    CONSTRAINT uq_categories_depenses_nom UNIQUE (nom)
);

CREATE TABLE depenses (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    montant NUMERIC(12, 2) NOT NULL,
    type_depense VARCHAR(20) NOT NULL,
    montant_par_personne NUMERIC(12, 2),
    description VARCHAR(255) NOT NULL,
    categorie_id BIGINT,
    statut VARCHAR(20) NOT NULL,
    cree_par BIGINT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    valide_par BIGINT,
    date_validation TIMESTAMP,
    CONSTRAINT fk_depenses_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_depenses_categorie FOREIGN KEY (categorie_id) REFERENCES categories_depenses (id),
    CONSTRAINT fk_depenses_cree_par FOREIGN KEY (cree_par) REFERENCES users (id),
    CONSTRAINT fk_depenses_valide_par FOREIGN KEY (valide_par) REFERENCES users (id),
    CONSTRAINT chk_depenses_montant CHECK (montant > 0),
    CONSTRAINT chk_depenses_type_montant_par_personne CHECK (
        (type_depense = 'CAGNOTTE' AND montant_par_personne IS NULL)
        OR (type_depense = 'PARTAGE' AND montant_par_personne IS NOT NULL AND montant_par_personne > 0)
    )
);

CREATE TABLE paiements (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT NOT NULL,
    residence_id BIGINT NOT NULL,
    nombre_mois INTEGER,
    montant_mensuel NUMERIC(12, 2) NOT NULL,
    montant_total NUMERIC(12, 2) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    date_paiement TIMESTAMP NOT NULL,
    type_paiement VARCHAR(30) NOT NULL,
    depense_id BIGINT,
    cree_par BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_paiements_logement FOREIGN KEY (logement_id) REFERENCES logements (id),
    CONSTRAINT fk_paiements_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_paiements_depense FOREIGN KEY (depense_id) REFERENCES depenses (id),
    CONSTRAINT fk_paiements_cree_par FOREIGN KEY (cree_par) REFERENCES users (id),
    CONSTRAINT chk_paiements_montants CHECK (montant_mensuel > 0 AND montant_total > 0),
    CONSTRAINT chk_paiements_type_depense CHECK (
        (type_paiement = 'CAGNOTTE' AND depense_id IS NULL)
        OR (type_paiement = 'DEPENSE_PARTAGE' AND depense_id IS NOT NULL)
    ),
    CONSTRAINT chk_paiements_nombre_mois CHECK (
        (type_paiement = 'CAGNOTTE' AND nombre_mois IS NOT NULL AND nombre_mois > 0)
        OR (type_paiement = 'DEPENSE_PARTAGE' AND nombre_mois IS NULL)
    )
);

CREATE TABLE payment_months (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT NOT NULL,
    month VARCHAR(7) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_id BIGINT,
    CONSTRAINT fk_payment_months_logement FOREIGN KEY (logement_id) REFERENCES logements (id),
    CONSTRAINT fk_payment_months_payment FOREIGN KEY (payment_id) REFERENCES paiements (id),
    CONSTRAINT uk_payment_months_logement_month UNIQUE (logement_id, month)
);

CREATE TABLE transactions_cagnotte (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    logement_id BIGINT,
    type VARCHAR(20) NOT NULL,
    montant NUMERIC(12, 2) NOT NULL,
    reference_id BIGINT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    CONSTRAINT fk_transactions_cagnotte_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_transactions_cagnotte_logement FOREIGN KEY (logement_id) REFERENCES logements (id),
    CONSTRAINT chk_transactions_cagnotte_montant CHECK (montant > 0)
);

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
    commentaire VARCHAR(1000),
    date_vote TIMESTAMP NOT NULL,
    CONSTRAINT fk_vote_utilisateurs_vote FOREIGN KEY (vote_id) REFERENCES votes (id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_utilisateurs_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES users (id),
    CONSTRAINT uk_vote_utilisateurs_vote_utilisateur UNIQUE (vote_id, utilisateur_id)
);

CREATE INDEX idx_logements_residence_id ON logements (residence_id);
CREATE INDEX idx_logements_residence_active ON logements (residence_id, active);

CREATE INDEX idx_users_residence_id ON users (residence_id);
CREATE INDEX idx_users_logement_id ON users (logement_id);
CREATE INDEX idx_users_role ON users (role);

CREATE INDEX idx_categories_depenses_nom ON categories_depenses (nom);

CREATE INDEX idx_depenses_residence_id ON depenses (residence_id);
CREATE INDEX idx_depenses_statut ON depenses (statut);
CREATE INDEX idx_depenses_type_depense ON depenses (type_depense);
CREATE INDEX idx_depenses_categorie_id ON depenses (categorie_id);

CREATE INDEX idx_paiements_logement_id ON paiements (logement_id);
CREATE INDEX idx_paiements_residence_id ON paiements (residence_id);
CREATE INDEX idx_paiements_depense_id ON paiements (depense_id);
CREATE INDEX idx_paiements_status ON paiements (status);
CREATE INDEX idx_paiements_type_paiement ON paiements (type_paiement);
CREATE INDEX idx_paiements_date_fin ON paiements (date_fin);
CREATE INDEX idx_paiements_depense_logement_status ON paiements (depense_id, logement_id, status);

CREATE INDEX idx_payment_months_payment_id ON payment_months (payment_id);
CREATE INDEX idx_payment_months_month ON payment_months (month);

CREATE INDEX idx_transactions_cagnotte_residence_id ON transactions_cagnotte (residence_id);
CREATE INDEX idx_transactions_cagnotte_logement_id ON transactions_cagnotte (logement_id);
CREATE INDEX idx_transactions_cagnotte_type ON transactions_cagnotte (type);

CREATE INDEX idx_votes_residence_id ON votes (residence_id);
CREATE INDEX idx_votes_statut_date_fin ON votes (statut, date_fin);

CREATE INDEX idx_vote_utilisateurs_vote_id ON vote_utilisateurs (vote_id);
CREATE INDEX idx_vote_utilisateurs_utilisateur_id ON vote_utilisateurs (utilisateur_id);
