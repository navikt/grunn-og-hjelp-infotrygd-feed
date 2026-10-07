package no.nav.grunn.og.hjelpestonad.infotrygd.feed.service

import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.ElementMetadata
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.FeedElement
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.FeedMeldingDto
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.GrunnstønadStartBehandlingRequest
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.GrunnstønadVedtakRequest
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.Innhold
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.InnholdStartBehandling
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.InnholdVedtak
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadFeedRepository
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadType
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@Service
class GrunnstønadInfotrygdFeedService(
    private val feedRepository: GrunnstønadFeedRepository,
    private val objectMapper: ObjectMapper,
) {
    fun opprettStartBehandling(melding: GrunnstønadStartBehandlingRequest) {
        lagre(
            type = GrunnstønadType.GS_StartBeh,
            personIdent = melding.personIdent,
            datoStartNyGrunnstønad = null,
            innhold = InnholdStartBehandling(personIdent = melding.personIdent),
        )
    }

    fun opprettVedtak(melding: GrunnstønadVedtakRequest) {
        lagre(
            type = GrunnstønadType.GS_Vedtak,
            personIdent = melding.personIdent,
            datoStartNyGrunnstønad = melding.datoStartNyGrunnstønad,
            innhold =
                InnholdVedtak(
                    personIdent = melding.personIdent,
                    datoStartNyGrunnstønad = melding.datoStartNyGrunnstønad,
                ),
        )
    }

    fun hentMeldingerFraFeed(sistLesteSekvensId: Long): FeedMeldingDto {
        val rader =
            feedRepository.hentMeldingerEtter(
                sistLesteSekvensId = sistLesteSekvensId,
                maxSize = MAKS_ANTALL_ELEMENTER,
            )

        return FeedMeldingDto(
            tittel = "Grunnstønad Infotrygd-feed",
            inneholderFlereElementer = rader.size > 1,
            elementer =
                rader.map {
                    FeedElement(
                        sekvensId = it.sekvensId,
                        type = it.type,
                        metadata = ElementMetadata(opprettetDato = it.opprettetDato),
                        innhold =
                            when (it.type) {
                                GrunnstønadType.GS_Vedtak -> {
                                    objectMapper.readValue(
                                        it.innhold,
                                        InnholdVedtak::class.java,
                                    )
                                }

                                GrunnstønadType.GS_StartBeh -> {
                                    objectMapper.readValue(
                                        it.innhold,
                                        InnholdStartBehandling::class.java,
                                    )
                                }
                            },
                    )
                },
        )
    }

    private fun lagre(
        type: GrunnstønadType,
        personIdent: String,
        datoStartNyGrunnstønad: LocalDate?,
        innhold: Innhold,
    ) {
        feedRepository.lagre(
            type = type,
            personIdent = personIdent,
            datoStartNyGrunnstønad = datoStartNyGrunnstønad,
            innhold = objectMapper.writeValueAsString(innhold),
        )
    }

    companion object {
        const val MAKS_ANTALL_ELEMENTER = 100
    }
}
