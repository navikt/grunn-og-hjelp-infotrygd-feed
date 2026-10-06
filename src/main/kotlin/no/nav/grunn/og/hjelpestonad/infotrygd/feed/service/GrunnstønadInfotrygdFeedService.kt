package no.nav.grunn.og.hjelpestonad.infotrygd.feed.service

import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.ElementMetadata
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.FeedElement
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.FeedMeldingDto
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadFeedRepository
import org.springframework.stereotype.Service

@Service
class GrunnstønadInfotrygdFeedService(
    private val feedRepository: GrunnstønadFeedRepository,
) {
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
                        innhold = it.innhold,
                    )
                },
        )
    }

    companion object {
        const val MAKS_ANTALL_ELEMENTER = 100
    }
}
