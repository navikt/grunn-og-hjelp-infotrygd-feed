package no.nav.grunn.og.hjelpestonad.infotrygd.feed.api

import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadType
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime

data class FeedMeldingDto(
    val elementer: List<FeedElement>,
    val inneholderFlereElementer: Boolean,
    val tittel: String,
)

data class FeedElement(
    val sekvensId: Long,
    val type: GrunnstønadType,
    val metadata: ElementMetadata,
    val innhold: JsonNode,
)

data class ElementMetadata(
    val opprettetDato: LocalDateTime,
)
