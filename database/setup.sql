-- Run with psql connected to the postgres database as an administrator.
-- Existing roles and databases are left unchanged.
SELECT 'CREATE ROLE student LOGIN PASSWORD ''chocolatefrog'''
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'student')
\gexec

SELECT 'CREATE DATABASE hogwarts OWNER student'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'hogwarts')
\gexec
