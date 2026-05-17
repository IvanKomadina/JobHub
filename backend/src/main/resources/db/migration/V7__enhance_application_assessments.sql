-- ============================================================
-- V6 - Enhance application assessments for AI pipeline
-- ============================================================

-- Drop old simple assessment table and recreate with full structure
ALTER TABLE application_assessments
    ADD COLUMN skills_match      TEXT,
ADD COLUMN skills_gap        TEXT,
ADD COLUMN experience_assessment TEXT,
ADD COLUMN education_assessment  TEXT,
ADD COLUMN explanation       TEXT,
ADD COLUMN recommendation    VARCHAR(50)
    CHECK (recommendation IN ('RECOMMENDED', 'CONSIDER', 'NOT_RECOMMENDED')),
ADD COLUMN semantic_score    NUMERIC(5,2),
ADD COLUMN skills_score      NUMERIC(5,2),
ADD COLUMN experience_score  NUMERIC(5,2),
ADD COLUMN education_score   NUMERIC(5,2);