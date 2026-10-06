# grunn-og-hjelp-infotrygd-feed

App som publiserer grunn- og hjelpestønad-hendelser til Infotrygd.

## Feed-API

`GET /api/grunnstonad/v1/feed?sistLesteSekvensId=<sekvensnummer>` returnerer hendelser med
høyere sekvensnummer, sortert stigende. Hvert kall henter maksimalt 100 elementer.
API-et følger responsformatet fra familie-feedene; hendelsestype og innhold holdes
generiske inntil kontrakten for grunn- og hjelpestønad-hendelser er avklart.

Tilgang fra `infotrygd-feed-proxy-v2` er konfigurert i `.nais/dev.yaml`.
Grunnstønad lagres i den egne `grunnstonad_feed`-tabellen; hjelpestønad kan senere
få en separat `hjelpestonad_feed`-tabell. Grunnstønad-feedtabellen er opprettet,
med `type`, `person_ident` og `dato_start_ny_grunnstonad`; den vil være tom inntil
innskriving av hendelser er implementert.

Bygg-workflowen verifiserer pull requests og push til brancher uten å deploye.
Dev-deploy startes manuelt fra `main` med `Deploy to dev`. Flyway er deaktivert i
dev inntil databasemigrasjonene er klare til å kjøres.
