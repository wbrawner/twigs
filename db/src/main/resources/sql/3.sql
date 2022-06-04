CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    id
    TEXT
    PRIMARY
    KEY,
    user_id
    TEXT
    NOT
    NULL,
    expiration
    TIMESTAMP
    NOT
    NULL,
    CONSTRAINT
    fk_users
    FOREIGN
    KEY
(
    user_id
) REFERENCES users
(
    id
) ON DELETE CASCADE
    );
