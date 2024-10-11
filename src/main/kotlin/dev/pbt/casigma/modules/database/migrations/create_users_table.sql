CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(50) NOT NULL, password text NOT NULL, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL);
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);
