ALTER TABLE users
    ADD COLUMN date_entree_residence DATE;

UPDATE users
SET date_entree_residence = CURRENT_DATE
WHERE date_entree_residence IS NULL;

ALTER TABLE users
    ALTER COLUMN date_entree_residence SET NOT NULL;

ALTER TABLE paiements
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'VALIDATED';

CREATE INDEX idx_paiements_status ON paiements (status);

CREATE TABLE payment_months (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    month VARCHAR(7) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_id BIGINT,
    CONSTRAINT fk_payment_months_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_payment_months_payment FOREIGN KEY (payment_id) REFERENCES paiements (id),
    CONSTRAINT uk_payment_months_user_month UNIQUE (user_id, month)
);

CREATE INDEX idx_payment_months_user_id ON payment_months (user_id);
CREATE INDEX idx_payment_months_payment_id ON payment_months (payment_id);
CREATE INDEX idx_payment_months_month ON payment_months (month);
