package no.nav.grunn.og.hjelpestonad.infotrygd.feed.service

import jakarta.validation.Validator
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.Application
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.GrunnstønadStartBehandlingRequest
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.GrunnstønadVedtakRequest
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.InnholdStartBehandling
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.api.InnholdVedtak
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.database.GrunnstønadType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.ActiveProfiles
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@SpringBootTest(classes = [Application::class])
@ActiveProfiles("test")
class GrunnstønadInfotrygdFeedServiceIntegrationTest {
    @Autowired
    private lateinit var service: GrunnstønadInfotrygdFeedService

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var validator: Validator

    @BeforeEach
    fun klargjorDatabase() {
        jdbcClient.sql("DELETE FROM grunnstonad_feed").update()
    }

    @Test
    fun `lagrer vedtak i database og returnerer det i feeden`() {
        val melding =
            GrunnstønadVedtakRequest(
                personIdent = "12345678901",
                datoStartNyGrunnstønad = LocalDate.parse("2026-01-01"),
            )

        service.opprettVedtak(melding)

        val lagret =
            jdbcClient
                .sql("SELECT type, person_ident, dato_start_ny_grunnstonad FROM grunnstonad_feed")
                .query { rs, _ ->
                    Triple(
                        rs.getString("type"),
                        rs.getString("person_ident"),
                        rs.getDate("dato_start_ny_grunnstonad")?.toLocalDate(),
                    )
                }.single()
        val feed = service.hentMeldingerFraFeed(sistLesteSekvensId = 0)

        assertEquals(GrunnstønadType.GS_Vedtak.name, lagret.first)
        assertEquals("12345678901", lagret.second)
        assertEquals(LocalDate.parse("2026-01-01"), lagret.third)
        assertEquals(1, feed.elementer.size)
        assertEquals(GrunnstønadType.GS_Vedtak, feed.elementer.single().type)
        assertEquals(
            InnholdVedtak(
                personIdent = "12345678901",
                datoStartNyGrunnstønad = LocalDate.parse("2026-01-01"),
            ),
            feed.elementer.single().innhold,
        )
        assertEquals(
            objectMapper.readTree("""{"personIdent":"12345678901","datoStartNyGrunnstønad":"2026-01-01"}"""),
            objectMapper
                .readTree(objectMapper.writeValueAsString(feed))
                .at("/elementer/0/innhold"),
        )
        assertFalse(feed.inneholderFlereElementer)
    }

    @Test
    fun `lagrer startbehandling uten startdato`() {
        service.opprettStartBehandling(GrunnstønadStartBehandlingRequest(personIdent = "12345678901"))

        val lagret =
            jdbcClient
                .sql("SELECT type, person_ident, dato_start_ny_grunnstonad FROM grunnstonad_feed")
                .query { rs, _ ->
                    Triple(
                        rs.getString("type"),
                        rs.getString("person_ident"),
                        rs.getDate("dato_start_ny_grunnstonad")?.toLocalDate(),
                    )
                }.single()

        assertEquals(GrunnstønadType.GS_StartBeh.name, lagret.first)
        assertEquals("12345678901", lagret.second)
        assertEquals(null, lagret.third)

        val feed = service.hentMeldingerFraFeed(sistLesteSekvensId = 0)
        assertEquals(GrunnstønadType.GS_StartBeh, feed.elementer.single().type)
        assertEquals(
            InnholdStartBehandling(personIdent = "12345678901"),
            feed.elementer.single().innhold,
        )
        assertEquals(
            objectMapper.readTree("""{"personIdent":"12345678901"}"""),
            objectMapper
                .readTree(objectMapper.writeValueAsString(feed))
                .at("/elementer/0/innhold"),
        )
    }

    @Test
    fun `henter stigende sekvensnummer etter markør og begrenser antall`() {
        repeat(GrunnstønadInfotrygdFeedService.MAKS_ANTALL_ELEMENTER + 1) {
            service.opprettStartBehandling(GrunnstønadStartBehandlingRequest(personIdent = "12345678901"))
        }

        val feed = service.hentMeldingerFraFeed(sistLesteSekvensId = 0)
        val nesteSide = service.hentMeldingerFraFeed(sistLesteSekvensId = feed.elementer.last().sekvensId)

        assertEquals(GrunnstønadInfotrygdFeedService.MAKS_ANTALL_ELEMENTER, feed.elementer.size)
        assertTrue(feed.inneholderFlereElementer)
        assertTrue(
            feed.elementer
                .map { it.sekvensId }
                .zipWithNext()
                .all { (a, b) -> a < b },
        )
        assertEquals(1, nesteSide.elementer.size)
        assertTrue(nesteSide.elementer.single().sekvensId > feed.elementer.last().sekvensId)
        assertFalse(nesteSide.inneholderFlereElementer)
    }

    @Test
    fun `tom feed inneholder ingen elementer`() {
        val feed = service.hentMeldingerFraFeed(sistLesteSekvensId = 0)

        assertTrue(feed.elementer.isEmpty())
        assertFalse(feed.inneholderFlereElementer)
    }

    @Test
    fun `validerer personident for begge meldingstyper`() {
        val ugyldigVedtak =
            GrunnstønadVedtakRequest(
                personIdent = "1234567890x",
                datoStartNyGrunnstønad = LocalDate.parse("2026-01-01"),
            )
        val ugyldigStartbehandling = GrunnstønadStartBehandlingRequest(personIdent = "123")
        val gyldigStartbehandling = GrunnstønadStartBehandlingRequest(personIdent = "12345678901")

        assertEquals(setOf("personIdent"), validator.validate(ugyldigVedtak).map { it.propertyPath.toString() }.toSet())
        assertEquals(
            setOf("personIdent"),
            validator.validate(ugyldigStartbehandling).map { it.propertyPath.toString() }.toSet(),
        )
        assertTrue(validator.validate(gyldigStartbehandling).isEmpty())
    }
}
