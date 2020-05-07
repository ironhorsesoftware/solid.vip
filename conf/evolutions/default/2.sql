-- Migration to the Silhouette-Persistence library.

-- !Ups

ALTER TABLE credentials ADD COLUMN id SERIAL;
ALTER TABLE credentials ADD COLUMN provider_id TEXT NOT NULL DEFAULT 'credentials';

ALTER TABLE credentials DROP CONSTRAINT credentials_pkey;
ALTER TABLE credentials ADD PRIMARY KEY (id);

ALTER TABLE credentials RENAME TO credentials_password;

CREATE TABLE credentials_oauth1 (
    id SERIAL PRIMARY KEY,
    provider_id TEXT NOT NULL,
    provider_key TEXT NOT NULL,
    token TEXT NOT NULL,
    secret TEXT NOT NULL
);

CREATE TABLE credentials_oauth2 (
    id SERIAL PRIMARY KEY,
    provider_id TEXT NOT NULL,
    provider_key TEXT NOT NULL,
    access_token TEXT NOT NULL,
    token_type TEXT,
    expires_in INTEGER,
    refresh_token TEXT,
    params TEXT
);

CREATE TABLE credentials_openid (
    id SERIAL PRIMARY KEY,
    provider_id TEXT NOT NULL,
    provider_key TEXT NOT NULL,
    openid TEXT NOT NULL,
    attributes TEXT NOT NULL
);

CREATE TABLE authentication_cookies (
    id SERIAL PRIMARY KEY,
    authenticator_id TEXT NOT NULL,
    provider_id TEXT NOT NULL,
    provider_key TEXT NOT NULL,
    last_used_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    idle_timeout BIGINT,
    max_age BIGINT,
    fingerprint TEXT
);

-- !Downs

DROP TABLE authentication_cookies;
DROP TABLE credentials_openid;
DROP TABLE credentials_oauth2;
DROP TABLE credentials_oauth1;

ALTER TABLE credentials_password RENAME TO credentials;

ALTER TABLE credentials DROP CONSTRAINT credentials_pkey;
ALTER TABLE credentials ADD PRIMARY KEY (provider_key);

ALTER TABLE credentials DROP COLUMN provider_id;
ALTER TABLE credentials DROP COLUMN id;