CREATE TABLE IF NOT EXISTS app_user (
                                        id            UUID PRIMARY KEY,
                                        email         VARCHAR(255) NOT NULL UNIQUE,
                                        password_hash VARCHAR(255) NOT NULL,
                                        created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS portfolio (
                                         id         UUID PRIMARY KEY,
                                         user_id    UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                         name       VARCHAR(120) NOT NULL,
                                         created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);