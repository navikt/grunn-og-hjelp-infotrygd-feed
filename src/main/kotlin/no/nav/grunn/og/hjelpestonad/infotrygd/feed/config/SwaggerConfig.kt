package no.nav.grunn.og.hjelpestonad.infotrygd.feed.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig(
    @param:Value("\${azure.authorization-url}")
    private val authorizationUrl: String,
    @param:Value("\${azure.token-endpoint-url}")
    private val tokenUrl: String,
    @param:Value("\${azure.api-scope}")
    private val apiScope: String,
) {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .components(Components().addSecuritySchemes("oauth2", oauth2SecurityScheme()))
            .addSecurityItem(SecurityRequirement().addList("oauth2", listOf(apiScope)))
            .info(
                Info()
                    .title("Grunn- og hjelpestønad Infotrygd feed")
                    .description("API for grunn- og hjelpestønad Infotrygd feed")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Team grunn- og hjelpestønad")
                            .url("https://github.com/navikt/grunn-og-hjelpestonad"),
                    ),
            )

    private fun oauth2SecurityScheme(): SecurityScheme =
        SecurityScheme()
            .name("oauth2")
            .type(SecurityScheme.Type.OAUTH2)
            .flows(
                OAuthFlows()
                    .authorizationCode(
                        OAuthFlow()
                            .authorizationUrl(authorizationUrl)
                            .tokenUrl(tokenUrl)
                            .scopes(Scopes().addString(apiScope, "API-tilgang")),
                    ),
            )
}
