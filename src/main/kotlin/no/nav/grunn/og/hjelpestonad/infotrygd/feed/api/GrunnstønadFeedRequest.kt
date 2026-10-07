package no.nav.grunn.og.hjelpestonad.infotrygd.feed.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class GrunnstønadVedtakRequest(
    @field:Pattern(regexp = "\\d{11}")
    val personIdent: String,
    val datoStartNyGrunnstønad: LocalDate,
)

data class GrunnstønadStartBehandlingRequest(
    @field:Pattern(regexp = "\\d{11}")
    val personIdent: String,
)
