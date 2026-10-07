package no.nav.grunn.og.hjelpestonad.infotrygd.feed.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/internal/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, FEED_PATH)
                    .hasRole(READ_FEED_ROLE)
                    .requestMatchers(HttpMethod.POST, "$FEED_PATH/**")
                    .hasRole(WRITE_FEED_ROLE)
                    .anyRequest()
                    .denyAll()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }.csrf { it.disable() }
            .build()

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter =
            JwtGrantedAuthoritiesConverter().apply {
                setAuthoritiesClaimName(ROLES_CLAIM)
                setAuthorityPrefix(ROLE_PREFIX)
            }
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        }
    }

    companion object {
        private const val FEED_PATH = "/api/grunnstonad/v1/feed"
        private const val ROLES_CLAIM = "roles"
        private const val ROLE_PREFIX = "ROLE_"
        private const val READ_FEED_ROLE = "read_feed"
        private const val WRITE_FEED_ROLE = "write_feed"
    }
}
