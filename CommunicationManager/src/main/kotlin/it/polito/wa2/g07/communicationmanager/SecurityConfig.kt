package it.polito.wa2.g07.communicationmanager

import org.apache.camel.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain


@org.springframework.context.annotation.Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun jwtAuthenticationConverter() : JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter{ jwt ->
            val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
            val crmClient = resourceAccess?.get("crmclient") as? Map<*, *>
            val roles = crmClient?.get("roles") as? Collection<*> ?: listOf<SimpleGrantedAuthority>()
            roles
                .map { "ROLE_$it" }
                .map { SimpleGrantedAuthority(it) }
        }
        return converter
    }

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { it.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()
    }
}






