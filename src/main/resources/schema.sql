CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    telegram_chat_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS otp_config (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    code_length INTEGER NOT NULL,
    ttl_seconds INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO otp_config (id, code_length, ttl_seconds)
VALUES (1, 6, 300)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS otp_codes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(255) NOT NULL,
    code_hash TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    delivery_channel VARCHAR(20) NOT NULL,
    destination VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_otp_codes_user_operation_status
ON otp_codes (user_id, operation_id, status);
