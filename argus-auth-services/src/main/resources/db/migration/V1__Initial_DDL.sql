-- Table Definitions

--
-- One Time Passwords for Integrations
--
CREATE TABLE one_time_passwords
(
    id            BIGSERIAL,
    uuid          UUID NOT NULL,
    pass          VARCHAR(32) NOT NULL,
    expiration    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT    one_time_passwords_user_unique UNIQUE (uuid),
    PRIMARY KEY   (id)
);

--
-- Banned Users
--
CREATE TABLE banned_users
(
    id            BIGSERIAL,
    uuid          UUID NOT NULL,
    reason        VARCHAR(256) NOT NULL,
    CONSTRAINT    banned_users_uuid_unique UNIQUE (uuid),
    PRIMARY KEY   (id)
);

--
-- Discord Users
--
CREATE TABLE discord_users
(
    id            BIGSERIAL,
    discord_id    VARCHAR(19) NOT NULL,
    uuid          UUID NOT NULL,
    PRIMARY KEY   (id)
);
