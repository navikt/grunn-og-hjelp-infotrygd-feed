package no.nav.grunn.og.hjelpestonad.infotrygd.feed.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 -> oauth2.jwt {} }
            .csrf { it.disable() }
            .build()
}
