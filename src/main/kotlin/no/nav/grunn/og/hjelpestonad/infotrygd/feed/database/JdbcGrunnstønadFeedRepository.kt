package no.nav.grunn.og.hjelpestonad.infotrygd.feed.database

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

@Repository
class JdbcGrunnstønadFeedRepository(
    private val jdbcClient: JdbcClient,
    private val objectMapper: ObjectMapper,
) : GrunnstønadFeedRepository {
    override fun hentMeldingerEtter(
        sistLesteSekvensId: Long,
        maxSize: Int,
    ): List<GrunnstønadFeed> =
        jdbcClient
            .sql(
                """
                SELECT sekvens_id, type, person_ident, dato_start_ny_grunnstonad, opprettet_dato, innhold_json
                FROM grunnstonad_feed
                WHERE sekvens_id > :sistLesteSekvensId
                ORDER BY sekvens_id ASC
                LIMIT :maxSize
                """.trimIndent(),
            ).param("sistLesteSekvensId", sistLesteSekvensId)
            .param("maxSize", maxSize)
            .query { rs, _ ->
                GrunnstønadFeed(
                    sekvensId = rs.getLong("sekvens_id"),
                    type = GrunnstønadType.valueOf(rs.getString("type")),
                    personIdent = rs.getString("person_ident"),
                    datoStartNyGrunnstønad = rs.getDate("dato_start_ny_grunnstonad")?.toLocalDate(),
                    opprettetDato = rs.getTimestamp("opprettet_dato").toLocalDateTime(),
                    innhold = objectMapper.readTree(rs.getString("innhold_json")),
                )
            }.list()
}
