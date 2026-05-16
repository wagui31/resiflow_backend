CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    residence_id BIGINT NOT NULL,
    type VARCHAR(80) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    related_entity_type VARCHAR(80),
    related_entity_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_residence FOREIGN KEY (residence_id) REFERENCES residences (id),
    CONSTRAINT fk_notifications_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE notification_recipients (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    CONSTRAINT fk_notification_recipients_notification FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_recipients_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_notification_recipients_notification_user UNIQUE (notification_id, user_id)
);

CREATE INDEX idx_notifications_residence_created_at ON notifications (residence_id, created_at DESC);
CREATE INDEX idx_notifications_type_created_at ON notifications (type, created_at DESC);
CREATE INDEX idx_notification_recipients_user_read ON notification_recipients (user_id, is_read);
CREATE INDEX idx_notification_recipients_notification ON notification_recipients (notification_id);
