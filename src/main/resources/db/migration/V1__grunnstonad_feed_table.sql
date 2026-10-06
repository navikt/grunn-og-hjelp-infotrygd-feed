CREATE TABLE grunnstonad_feed
(
    sekvens_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type                        VARCHAR(100) NOT NULL,
    person_ident                VARCHAR(20)  NOT NULL,
    dato_start_ny_grunnstonad   DATE,
    innhold_json                TEXT         NOT NULL,
    opprettet_dato              TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
