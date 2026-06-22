-- ============================================================
-- V10 - Remove Location entity, add free text city/country
-- ============================================================

-- ===== JOB POSTS =====
ALTER TABLE job_posts ADD COLUMN city VARCHAR(100);
ALTER TABLE job_posts ADD COLUMN country VARCHAR(100);

ALTER TABLE job_posts DROP CONSTRAINT IF EXISTS fkjob_posts_location;
ALTER TABLE job_posts DROP COLUMN IF EXISTS location_id;

CREATE INDEX idx_job_posts_city ON job_posts(city);

-- ===== CANDIDATES =====
ALTER TABLE candidates ADD COLUMN city VARCHAR(100);
ALTER TABLE candidates ADD COLUMN country VARCHAR(100);
ALTER TABLE candidates DROP COLUMN IF EXISTS location;

-- ===== EMPLOYERS =====
ALTER TABLE employers ADD COLUMN city VARCHAR(100);
ALTER TABLE employers ADD COLUMN country VARCHAR(100);
ALTER TABLE employers DROP COLUMN IF EXISTS location;

-- ===== DROP LOCATIONS TABLE =====
DROP TABLE IF EXISTS locations;