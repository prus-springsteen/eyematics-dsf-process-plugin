#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE dic_a_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dic_a_fhir TO liquibase_user;
    CREATE DATABASE dic_a_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dic_a_bpe TO liquibase_user;
    CREATE DATABASE dic_c_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dic_c_fhir TO liquibase_user;
    CREATE DATABASE dic_c_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dic_c_bpe TO liquibase_user;
    CREATE DATABASE dic_b_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dic_b_fhir TO liquibase_user;
    CREATE DATABASE dic_b_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dic_b_bpe TO liquibase_user;
    CREATE DATABASE dic_d_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dic_d_fhir TO liquibase_user;
    CREATE DATABASE dic_d_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dic_d_bpe TO liquibase_user;
    CREATE DATABASE keycloak;
    GRANT ALL PRIVILEGES ON DATABASE keycloak TO liquibase_user;
EOSQL