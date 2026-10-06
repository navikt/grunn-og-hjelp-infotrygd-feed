package no.nav.grunn.og.hjelpestonad.infotrygd.feed.database

import tools.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime

interface GrunnstønadFeedRepository {
    fun hentMeldingerEtter(
        sistLesteSekvensId: Long,
        maxSize: Int,
    ): List<GrunnstønadFeed>
}

data class GrunnstønadFeed(
    val sekvensId: Long,
    val type: GrunnstønadType,
    val personIdent: String,
    val datoStartNyGrunnstønad: LocalDate?,
    val opprettetDato: LocalDateTime,
    val innhold: JsonNode,
)
