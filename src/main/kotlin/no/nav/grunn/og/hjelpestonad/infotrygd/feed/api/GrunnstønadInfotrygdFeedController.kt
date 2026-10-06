package no.nav.grunn.og.hjelpestonad.infotrygd.feed.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.PositiveOrZero
import no.nav.grunn.og.hjelpestonad.infotrygd.feed.service.GrunnstønadInfotrygdFeedService
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/grunnstonad/v1/feed")
class GrunnstønadInfotrygdFeedController(
    private val grunnstønadInfotrygdFeedService: GrunnstønadInfotrygdFeedService,
) {
    @Operation(
        summary = "Hent hendelser fra feeden",
        description = "Henter hendelser med sekvensId større enn sistLesteSekvensId.",
    )
    @GetMapping(produces = ["application/json"])
    fun hentFeed(
        @Parameter(description = "Sist leste sekvensnummer.", required = true, example = "0")
        @RequestParam("sistLesteSekvensId")
        @PositiveOrZero
        sistLesteSekvensId: Long,
    ): FeedMeldingDto {
        val feed = grunnstønadInfotrygdFeedService.hentMeldingerFraFeed(sistLesteSekvensId)
        logger.info("Hentet ${feed.elementer.size} feed-elementer etter sekvensnummer $sistLesteSekvensId")
        return feed
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GrunnstønadInfotrygdFeedController::class.java)
    }
}
