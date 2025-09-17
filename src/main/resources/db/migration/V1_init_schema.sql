-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TABLE chat_session (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255),
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);


-- Create chat_message table
CREATE TABLE chat_message (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL,
       role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM', 'AI')),
    content TEXT NOT NULL,
    retrieved_context TEXT,
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_chat_message_session FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
);

-- Create the exact index that matches your entity
CREATE INDEX idx_chat_message_session_created ON chat_message(session_id, created_at);

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers to auto-update updated_at and version on updates
CREATE TRIGGER update_chat_session_updated_at
    BEFORE UPDATE ON chat_session
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chat_message_updated_at
    BEFORE UPDATE ON chat_message
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();