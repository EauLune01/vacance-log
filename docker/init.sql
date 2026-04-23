/*
CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(1536)
);
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
ON vector_store
USING hnsw (embedding vector_cosine_ops);*/
