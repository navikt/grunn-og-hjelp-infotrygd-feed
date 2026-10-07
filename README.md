# grunn-og-hjelp-infotrygd-feed

App som publiserer grunn- og hjelpestønad-hendelser til Infotrygd.

## Feed-API

Kallet som skal brukes av infotrygd og infotrygd-proxy: `GET /api/grunnstonad/v1/feed?sistLesteSekvensId=<sekvensnummer>` 
denne returnerer hendelser med høyere sekvensnummer, sortert stigende. Hvert kall henter maksimalt 100 elementer.
API-et følger responsformatet fra familie-feedene og returnerer hendelsestype samt
typet innhold for vedtak og startbehandling. Innholdet lagres som JSONB i databasen
med `writeValueAsString` og leses til riktig innholdsmodell med `readValue`.

Grunnstønad-appen kan lagre hendelser med `POST /api/grunnstonad/v1/feed/vedtaksmelding`
(`personIdent`, `datoStartNyGrunnstønad`) og
`POST /api/grunnstonad/v1/feed/startbehandlingsmelding` (`personIdent`). Begge
endepunktene svarer `204 No Content`; opprettede hendelser får sekvensnummer fra databasen.

## Tester

Kjør `mvn verify` med Docker tilgjengelig. Integrasjonstestene starter PostgreSQL 18
automatisk via Testcontainers og kjører Flyway-migreringene mot denne databasen.
