CREATE TABLE urls (
  code VARCHAR PRIMARY KEY,
  address VARCHAR NOT NULL,
  hit BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
