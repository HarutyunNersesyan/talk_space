CREATE TABLE IF NOT EXISTS refresh_sessions (
 id varchar(255) PRIMARY KEY,
 email varchar(255) NOT NULL,
 token_hash varchar(64) NOT NULL,
 password_hash varchar(64) NOT NULL,
 expires_at timestamp with time zone NOT NULL,
 revoked boolean NOT NULL DEFAULT false
);
