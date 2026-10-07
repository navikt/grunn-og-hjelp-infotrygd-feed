package no.nav.grunn.og.hjelpestonad.infotrygd.feed.api

import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadType
import java.time.LocalDate
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
    val innhold: Innhold,
)

data class ElementMetadata(
    val opprettetDato: LocalDateTime,
)

interface Innhold

data class InnholdVedtak(
    val personIdent: String,
    val datoStartNyGrunnstønad: LocalDate,
) : Innhold

data class InnholdStartBehandling(
    val personIdent: String,
) : Innhold
