package no.nav.grunn.og.hjelpestonad.infotrygd.feed.config

import no.nav.grunn.og.hjelpestonad.infotrygd.feed.Application
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [Application::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @Autowired
    private lateinit var jwtAuthenticationConverter: JwtAuthenticationConverter

    @BeforeEach
    fun tømmerFeed() {
        jdbcClient.sql("DELETE FROM grunnstonad_feed").update()
    }

    @Test
    fun `leserolle kan hente feeden men ikke lagre`() {
        mockMvc
            .perform(
                get("/api/grunnstonad/v1/feed")
                    .param("sistLesteSekvensId", "0")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_read_feed"))),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/api/grunnstonad/v1/feed/vedtaksmelding")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"personIdent":"12345678901","datoStartNyGrunnstønad":"2026-01-01"}""")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_read_feed"))),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `skrivrolle kan lagre men ikke hente feeden`() {
        mockMvc
            .perform(
                post("/api/grunnstonad/v1/feed/vedtaksmelding")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"personIdent":"12345678901","datoStartNyGrunnstønad":"2026-01-01"}""")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_write_feed"))),
            ).andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/grunnstonad/v1/feed").param("sistLesteSekvensId", "0").with(
                    jwt().authorities(SimpleGrantedAuthority("ROLE_write_feed")),
                ),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `mapper roller fra jwt til prefiksede authorities`() {
        val jwt =
            Jwt
                .withTokenValue("test-token")
                .header("alg", "none")
                .claim("roles", listOf("read_feed", "write_feed", "access_as_application", "unknown"))
                .build()

        val authorities = checkNotNull(jwtAuthenticationConverter.convert(jwt)).authorities.map { it.authority }.toSet()

        assertEquals(
            setOf(
                "ROLE_read_feed",
                "ROLE_write_feed",
                "ROLE_access_as_application",
                "ROLE_unknown",
                "FACTOR_BEARER",
            ),
            authorities,
        )
    }
}
