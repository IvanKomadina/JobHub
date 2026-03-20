-- ============================================================
-- V1 - Initial Schema
-- ============================================================

-- ==================== TABLES ====================

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL CHECK (role IN ('ADMINISTRATOR', 'EMPLOYER', 'CANDIDATE')),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE candidates (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(30),
    location        VARCHAR(255),
    profile_picture VARCHAR(512),
    bio             TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE employers (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    industry     VARCHAR(100),
    website      VARCHAR(512),
    location     VARCHAR(255),
    logo_url     VARCHAR(512),
    description  TEXT,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE locations (
    id      BIGSERIAL    PRIMARY KEY,
    city    VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    UNIQUE (city, country)
);

CREATE TABLE job_posts (
    id              BIGSERIAL     PRIMARY KEY,
    employer_id     BIGINT        NOT NULL REFERENCES employers(id) ON DELETE CASCADE,
    category_id     BIGINT        REFERENCES categories(id) ON DELETE SET NULL,
    location_id     BIGINT        REFERENCES locations(id) ON DELETE SET NULL,
    title           VARCHAR(255)  NOT NULL,
    description     TEXT          NOT NULL,
    requirements    TEXT,
    employment_type VARCHAR(50)   CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'STUDENT')),
    salary_min      NUMERIC(12,2),
    salary_max      NUMERIC(12,2),
    status          VARCHAR(50)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'DELETED')),
    published_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    closes_at       TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_job_posts_status       ON job_posts(status);
CREATE INDEX idx_job_posts_category_id  ON job_posts(category_id);
CREATE INDEX idx_job_posts_location_id  ON job_posts(location_id);
CREATE INDEX idx_job_posts_employer_id  ON job_posts(employer_id);
CREATE INDEX idx_job_posts_published_at ON job_posts(published_at DESC);
CREATE INDEX idx_job_posts_title_fts    ON job_posts USING GIN (to_tsvector('english', title));

CREATE TABLE applications (
    id           BIGSERIAL   PRIMARY KEY,
    job_post_id  BIGINT      NOT NULL REFERENCES job_posts(id) ON DELETE CASCADE,
    candidate_id BIGINT      NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    cover_letter TEXT,
    applied_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_application_candidate_post UNIQUE (job_post_id, candidate_id)
);

CREATE INDEX idx_applications_candidate_id ON applications(candidate_id);
CREATE INDEX idx_applications_job_post_id  ON applications(job_post_id);

CREATE TABLE application_documents (
    id             BIGSERIAL    PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    file_name      VARCHAR(255) NOT NULL,
    file_url       VARCHAR(512) NOT NULL,
    file_type      VARCHAR(50)  CHECK (file_type IN ('RESUME', 'COVER_LETTER', 'CERTIFICATE', 'OTHER')),
    uploaded_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE application_assessments (
    id             BIGSERIAL    PRIMARY KEY,
    application_id BIGINT       NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    match_score    NUMERIC(5,2) CHECK (match_score >= 0 AND match_score <= 100),
    employer_notes TEXT,
    assessed_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE resumes (
    id           BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT    NOT NULL UNIQUE REFERENCES candidates(id) ON DELETE CASCADE,
    summary      TEXT,
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE resume_education (
    id             BIGSERIAL    PRIMARY KEY,
    resume_id      BIGINT       NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    institution    VARCHAR(255) NOT NULL,
    degree         VARCHAR(255),
    field_of_study VARCHAR(255),
    start_date     DATE,
    end_date       DATE,
    description    TEXT,
    sort_order     INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE resume_experience (
    id          BIGSERIAL    PRIMARY KEY,
    resume_id   BIGINT       NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    company     VARCHAR(255) NOT NULL,
    position    VARCHAR(255) NOT NULL,
    location    VARCHAR(255),
    start_date  DATE,
    end_date    DATE,
    description TEXT,
    sort_order  INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE resume_skills (
    id           BIGSERIAL   PRIMARY KEY,
    resume_id    BIGINT      NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_name   VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    skill_level  VARCHAR(50) CHECK (skill_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    sort_order   INTEGER     NOT NULL DEFAULT 0,
    UNIQUE (resume_id, skill_name)
);

CREATE TABLE resume_languages (
    id             BIGSERIAL    PRIMARY KEY,
    resume_id      BIGINT       NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    language_name  VARCHAR(100) NOT NULL,
    display_name   VARCHAR(100) NOT NULL,
    language_level VARCHAR(50)  CHECK (language_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'NATIVE')),
    sort_order     INTEGER      NOT NULL DEFAULT 0,
    UNIQUE (resume_id, language_name)
);

CREATE TABLE favorites (
    id           BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT    NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    job_post_id  BIGINT    NOT NULL REFERENCES job_posts(id) ON DELETE CASCADE,
    saved_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (candidate_id, job_post_id)
);

-- ==================== VIEWS ====================

CREATE VIEW platform_statistics AS
SELECT
    (SELECT COUNT(*) FROM users WHERE role = 'CANDIDATE' AND is_active = TRUE) AS total_candidates,
    (SELECT COUNT(*) FROM users WHERE role = 'EMPLOYER'  AND is_active = TRUE) AS total_employers,
    (SELECT COUNT(*) FROM job_posts WHERE status = 'ACTIVE')                   AS active_posts,
    (SELECT COUNT(*) FROM job_posts)                                           AS total_posts,
    (SELECT COUNT(*) FROM applications)                                        AS total_applications;

CREATE VIEW applications_per_post AS
SELECT
    jp.id          AS post_id,
    jp.title,
    e.company_name,
    jp.status,
    COUNT(a.id)    AS application_count
FROM job_posts jp
JOIN employers e ON e.id = jp.employer_id
LEFT JOIN applications a ON a.job_post_id = jp.id
GROUP BY jp.id, jp.title, e.company_name, jp.status;
