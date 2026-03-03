-- CREATE USER bankrest WITH PASSWORD 'bankrest';
-- CREATE DATABASE bankrest;
-- GRANT CONNECT ON DATABASE bankrest TO bankrest;
-- GRANT ALL PRIVILEGES ON DATABASE bankrest to bankrest;

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

-- Назначение прав на таблицу
GRANT ALL PRIVILEGES ON TABLE customers TO bankrest;
GRANT USAGE, SELECT ON SEQUENCE customers_id_seq TO bankrest;