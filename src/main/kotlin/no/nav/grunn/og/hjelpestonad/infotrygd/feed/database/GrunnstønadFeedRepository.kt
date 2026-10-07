package no.nav.grunn.og.hjelpestonad.infotrygd.feed.database

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class GrunnstønadFeedRepository(
    private val jdbcClient: JdbcClient,
) {
    fun lagre(
        type: GrunnstønadType,
        personIdent: String,
        datoStartNyGrunnstønad: LocalDate?,
        innhold: String,
    ) {
        jdbcClient
            .sql(
                """
                INSERT INTO grunnstonad_feed (type, person_ident, dato_start_ny_grunnstonad, innhold)
                VALUES (:type, :personIdent, :datoStartNyGrunnstonad, :innhold)
                """.trimIndent(),
            ).param("type", type.name)
            .param("personIdent", personIdent)
            .param("datoStartNyGrunnstonad", datoStartNyGrunnstønad)
            .param("innhold", innhold, Types.OTHER)
            .update()
    }

    fun hentMeldingerEtter(
        sistLesteSekvensId: Long,
        maxSize: Int,
    ): List<GrunnstønadFeed> =
        jdbcClient
            .sql(
                """
                SELECT sekvens_id, type, person_ident, dato_start_ny_grunnstonad, opprettet_dato, innhold
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
                    innhold = rs.getString("innhold"),
                )
            }.list()
}

data class GrunnstønadFeed(
    val sekvensId: Long,
    val type: GrunnstønadType,
    val personIdent: String,
    val datoStartNyGrunnstønad: LocalDate?,
    val opprettetDato: LocalDateTime,
    val innhold: String,
)
