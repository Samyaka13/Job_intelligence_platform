ALTER TABLE candidate_profiles
    ADD COLUMN preferred_locations JSONB;

ALTER TABLE candidate_profiles
DROP COLUMN preferred_work_modes;