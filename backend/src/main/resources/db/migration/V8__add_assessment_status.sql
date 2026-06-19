ALTER TABLE application_assessments
    ADD COLUMN assessment_status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED'
        CHECK (assessment_status IN ('PENDING', 'GENERATING', 'COMPLETED', 'FAILED'));