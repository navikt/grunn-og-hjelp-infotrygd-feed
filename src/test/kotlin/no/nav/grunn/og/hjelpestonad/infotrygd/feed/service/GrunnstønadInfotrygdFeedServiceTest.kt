package no.nav.grunn.og.hjelpestonad.infotrygd.feed.service

import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadFeed
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadFeedRepository
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

class GrunnstønadInfotrygdFeedServiceTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `henter feed etter sist leste sekvensnummer med forventet sidegrense`() {
        val repository = StubGrunnstønadFeedRepository(listOf(feedRad(12), feedRad(14)))
        val service = GrunnstønadInfotrygdFeedService(repository)

        val resultat = service.hentMeldingerFraFeed(sistLesteSekvensId = 10)

        assertEquals(10, repository.sistLesteSekvensId)
        assertEquals(GrunnstønadInfotrygdFeedService.MAKS_ANTALL_ELEMENTER, repository.maxSize)
        assertEquals(listOf(12L, 14L), resultat.elementer.map { it.sekvensId })
        assertEquals(listOf(GrunnstønadType.GS_Vedtak, GrunnstønadType.GS_Vedtak), resultat.elementer.map { it.type })
        assertTrue(resultat.inneholderFlereElementer)
        assertEquals("Grunnstønad Infotrygd-feed", resultat.tittel)
    }

    @Test
    fun `tom feed har ingen flere elementer`() {
        val service = GrunnstønadInfotrygdFeedService(StubGrunnstønadFeedRepository(emptyList()))

        val resultat = service.hentMeldingerFraFeed(sistLesteSekvensId = 0)

        assertTrue(resultat.elementer.isEmpty())
        assertFalse(resultat.inneholderFlereElementer)
    }

    private fun feedRad(sekvensId: Long) =
        GrunnstønadFeed(
            sekvensId = sekvensId,
            type = GrunnstønadType.GS_Vedtak,
            personIdent = "12345678901",
            datoStartNyGrunnstønad = null,
            opprettetDato = LocalDateTime.parse("2026-01-01T12:00:00"),
            innhold = objectMapper.readTree("""{"eksempel":"verdi"}"""),
        )

    private class StubGrunnstønadFeedRepository(
        private val rader: List<GrunnstønadFeed>,
    ) : GrunnstønadFeedRepository {
        var sistLesteSekvensId: Long? = null
        var maxSize: Int? = null

        override fun hentMeldingerEtter(
            sistLesteSekvensId: Long,
            maxSize: Int,
        ): List<GrunnstønadFeed> {
            this.sistLesteSekvensId = sistLesteSekvensId
            this.maxSize = maxSize
            return rader
        }
    }
}
