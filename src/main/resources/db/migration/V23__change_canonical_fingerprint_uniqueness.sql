ALTER TABLE jobs
DROP CONSTRAINT uq_jobs_canonical_fingerprint;

CREATE INDEX idx_jobs_canonical_fingerprint
    ON jobs (canonical_fingerprint);