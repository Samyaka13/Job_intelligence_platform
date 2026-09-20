ALTER TABLE job_matches
    ADD COLUMN mandatory_skills JSONB NOT NULL DEFAULT '[]'::jsonb;

ALTER TABLE job_matches
    ADD COLUMN missing_mandatory_skills JSONB NOT NULL DEFAULT '[]'::jsonb;