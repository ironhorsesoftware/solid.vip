-- !Ups

CREATE TABLE profiles (
	id           SERIAL PRIMARY KEY,
    provider_id  TEXT   NOT NULL,
    provider_key TEXT   NOT NULL,
	profile_json TEXT   NOT NULL DEFAULT '{}'
);

-- !Downs

DROP TABLE profiles;