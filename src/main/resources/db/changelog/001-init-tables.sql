CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telegram_account VARCHAR(100),
    telegram_chat_id VARCHAR(100)
);

CREATE TABLE reminder (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(4096),
    remind TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sent BOOLEAN NOT NULL DEFAULT FALSE
);