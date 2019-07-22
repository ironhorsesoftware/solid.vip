-- Initial Schema

-- !Ups

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE members (
	id              UUID PRIMARY KEY UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
	username        TEXT                    NOT NULL,
	name            TEXT                    NOT NULL,
	email           TEXT,
	is_activated    BOOLEAN                 NOT NULL
);

CREATE TABLE credentials (
	provider_key    TEXT PRIMARY KEY UNIQUE NOT NULL,
	password        TEXT                    NOT NULL,
	password_hasher TEXT                    NOT NULL,
	password_salt   TEXT
);

-- !Downs

DROP TABLE credentials;
DROP TABLE members;

DROP EXTENSION IF EXISTS "uuid-ossp";
