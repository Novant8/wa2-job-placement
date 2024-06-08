package it.polito.wa2.g07.communicationmanager

import org.apache.camel.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter{
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter{
            jwt ->
            val  resourceAccess = jwt.claims["resource_access"]
            if (resourceAccess !is Map<*,*>) {
                error("resource_access is not a map")
            }
            val crmClient = resourceAccess["crmclient"]
            if(crmClient !is Map<*, *>) {
                error("crm_client is not a map")
            }
            val roles = crmClient["roles"]
            if(roles !is Collection<*>) {
                error("roles is not a collection")
            }
            roles
                .map { "ROLE$it"  }
                .map {SimpleGrantedAuthority(it)}
        }
        return converter
    }


    @Bean
    fun filterChain(httpSecurity: HttpSecurity):SecurityFilterChain{
        return httpSecurity
            .authorizeHttpRequests{
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer{
                it.jwt { it.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()
    }

}









