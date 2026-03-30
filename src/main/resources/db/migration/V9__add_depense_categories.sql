CREATE TABLE categories_depenses (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    CONSTRAINT uq_categories_depenses_nom UNIQUE (nom)
);

ALTER TABLE depenses
    ADD COLUMN categorie_id BIGINT;

ALTER TABLE depenses
    ADD CONSTRAINT fk_depenses_categorie
        FOREIGN KEY (categorie_id) REFERENCES categories_depenses (id);

CREATE INDEX idx_categories_depenses_nom ON categories_depenses (nom);
CREATE INDEX idx_depenses_categorie_id ON depenses (categorie_id);
