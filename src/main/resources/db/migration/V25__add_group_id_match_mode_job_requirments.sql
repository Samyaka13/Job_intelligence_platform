ALTER TABLE job_requirements
    ADD COLUMN group_id VARCHAR(100);

ALTER TABLE job_requirements
    ADD COLUMN match_mode VARCHAR(20) NOT NULL DEFAULT 'SINGLE';

ALTER TABLE job_requirements
    ADD CONSTRAINT chk_job_requirements_match_mode
        CHECK (match_mode IN ('SINGLE', 'ANY_OF', 'ALL_OF'));

CREATE INDEX idx_job_requirements_group_id
    ON job_requirements(group_id);