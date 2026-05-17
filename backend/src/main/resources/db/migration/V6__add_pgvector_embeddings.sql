-- ============================================================
-- V5 - pgvector embeddings and assessment enhancements
-- ============================================================

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding column to resumes
ALTER TABLE resumes
    ADD COLUMN embedding vector(1024);

-- Add embedding column to job_posts
ALTER TABLE job_posts
    ADD COLUMN embedding vector(1024);

-- Add index for fast similarity search on resumes
CREATE INDEX idx_resumes_embedding
    ON resumes USING hnsw (embedding vector_cosine_ops);

-- Add index for fast similarity search on job_posts
CREATE INDEX idx_job_posts_embedding
    ON job_posts USING hnsw (embedding vector_cosine_ops);