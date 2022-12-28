-- Table Definitions

--
-- Namespace Tables
--
CREATE TABLE namespaces
(
    id           BIGSERIAL,
    name         VARCHAR(20),
    PRIMARY KEY  (id)
);

CREATE TABLE namespace_mappings
(
    id            BIGSERIAL,
    namespace_id  BIGINT NOT NULL,
    mapping       VARCHAR(255) NOT NULL,
    PRIMARY KEY   (id),
    FOREIGN KEY   (namespace_id) REFERENCES namespaces(id)
);

--
-- Group Tables
--
CREATE TABLE groups
(
    id            BIGSERIAL,
    namespace_id  BIGINT NOT NULL,
    name          VARCHAR(20) NOT NULL,
    owner         UUID NOT NULL,
    metadata      JSONB NOT NULL DEFAULT '{}',
    PRIMARY KEY   (id),
    FOREIGN KEY   (namespace_id) REFERENCES namespaces(id)
);

CREATE TABLE user_permissions
(
    uuid         UUID NOT NULL,
    group_id     BIGINT NOT NULL,
    permission   INT NOT NULL DEFAULT 0,
    elected      INT NOT NULL DEFAULT 0,
    PRIMARY KEY  (uuid, group_id),
    FOREIGN KEY  (group_id) REFERENCES groups(id),
    CONSTRAINT   elected_allowed CHECK (elected <= permission)
);

CREATE TABLE group_audit
(
    id           BIGSERIAL,
    group_id     BIGINT NOT NULL,
    changer      UUID NOT NULL,
    target       UUID NOT NULL,
    act          INT NOT NULL,
    permission   INT NOT NULL,
    occurred     TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY  (id),
    FOREIGN KEY  (group_id) REFERENCES groups(id)
);
