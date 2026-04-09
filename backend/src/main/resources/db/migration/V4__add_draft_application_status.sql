-- ============================================================
-- V4 - Add DRAFT status to applications
-- ============================================================

ALTER TABLE applications
DROP CONSTRAINT applications_status_check;

ALTER TABLE applications
    ADD CONSTRAINT applications_status_check
        CHECK (status IN ('DRAFT', 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'));