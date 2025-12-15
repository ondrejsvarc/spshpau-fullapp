#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE "spshpau-project-db"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'spshpau-project-db')\gexec
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "spshpau-project-db" <<-EOSQL
    GRANT ALL PRIVILEGES ON DATABASE "spshpau-project-db" TO "$POSTGRES_USER";
EOSQL
