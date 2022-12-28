--
-- Indexes
--
-- Index used for searching group name based on all lowercase so names are case-insensitive
CREATE UNIQUE INDEX lower_case_group_name ON groups (namespace_id, LOWER(name));
