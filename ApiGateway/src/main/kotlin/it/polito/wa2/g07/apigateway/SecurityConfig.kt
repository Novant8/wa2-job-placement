package it.polito.wa2.g07.apigateway

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.util.Supplier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.csrf.CsrfTokenRequestHandler
import org.springframework.security.web.csrf.DefaultCsrfToken
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Configuration
class SecurityConfig(val crr: ClientRegistrationRepository) {

    fun oidcLogoutSuccessHandler() = OidcClientInitiatedLogoutSuccessHandler(crr)
        .also { it.setPostLogoutRedirectUri("http://localhost:8080/") }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/ui/**", "/", "/me","/logout").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login {  }
            .formLogin { it.disable() }
            .logout { it.logoutSuccessHandler(oidcLogoutSuccessHandler()) }
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                it.csrfTokenRequestHandler(SpaCsrfTokenRequestHandler())
            }
            .cors { it.disable() }
           // .addFilterAfter(CsrfCookieFilter(),BasicAuthenticationFilter::class.java)
            .build()
    }
}

class SpaCsrfTokenRequestHandler : CsrfTokenRequestAttributeHandler(){
    private val delegate:CsrfTokenRequestHandler = CsrfTokenRequestAttributeHandler()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        t: java.util.function.Supplier<CsrfToken>
    ) {
        delegate.handle(request, response, t)
    }

    override fun resolveCsrfTokenValue(request: HttpServletRequest, csrfToken: CsrfToken): String {
        val d = csrfToken as DefaultCsrfToken

        return if(StringUtils.hasText(request.getHeader(csrfToken.headerName) )){
            super.resolveCsrfTokenValue(request,csrfToken)
        }else{
            delegate.resolveCsrfTokenValue(request,csrfToken)
        }
    }
}

class CsrfCookieFilter: OncePerRequestFilter(){
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val csrfToken = request.getAttribute("_csfr") as CsrfToken
        //render the token value to a cookie by cousing the deferred token to be load
        csrfToken.token
        filterChain.doFilter(request,response)
    }
}