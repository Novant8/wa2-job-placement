package it.polito.wa2.g07.apigateway

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.csrf.CsrfTokenRequestHandler
import org.springframework.security.web.csrf.DefaultCsrfToken
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.function.Supplier
import jakarta.servlet.http.Cookie




class CustomLogoutHandler(private val cookieName: String) : LogoutHandler {
    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        // Remove the cookie by setting its max age to 0
        val cookie = Cookie(cookieName, null)
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }
}

@Configuration
class SecurityConfig(val crr: ClientRegistrationRepository) {

    companion object {
        val ROLE_REDIRECT_MAP = mapOf(
            "professional" to "http://localhost:8080/",
            "customer" to "http://localhost:8080/",
            "manager" to "http://localhost:8080/",
            "operator" to "http://localhost:8080/"
        )

        fun getRolesFromAuthentication(authentication: Authentication?): Collection<String> {
            val oAuth2AuthenticationToken = authentication as? OAuth2AuthenticationToken
            val resourceAccess = oAuth2AuthenticationToken?.principal?.attributes?.get("resource_access") as? Map<*, *>
            val crmClient = resourceAccess?.get("crmclient") as? Map<*, *>
            val roles = crmClient?.get("roles") as? Collection<*> ?: listOf<String>()
            return roles.map { it.toString() }
        }
    }

    fun oidcLogoutSuccessHandler() = OidcClientInitiatedLogoutSuccessHandler(crr)
        .also {

            it.setPostLogoutRedirectUri("http://localhost:8080/") }

    fun authSuccessHandler() = AuthenticationSuccessHandler { _, response, authentication ->
        val roles = getRolesFromAuthentication(authentication)
        val redirectLink = roles.firstNotNullOfOrNull { ROLE_REDIRECT_MAP[it] } ?: "http://localhost:8080/ui/edit-account"
        response.sendRedirect(redirectLink)
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/ui/edit-account").authenticated()
                it.requestMatchers("/ui/**", "/", "/me","/logout","/actuator/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login {
                it.successHandler(authSuccessHandler())
            }
            .formLogin { it.disable() }
            .logout {
                it.addLogoutHandler(CustomLogoutHandler("grafana_session"))
                it.addLogoutHandler(CustomLogoutHandler("grafana_session_expiry"))
                it.logoutSuccessHandler(oidcLogoutSuccessHandler())

            }
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                it.csrfTokenRequestHandler(SpaCsrfTokenRequestHandler())
            }
            .cors { it.disable() }
            .addFilterAfter(CsrfCookieFilter(),BasicAuthenticationFilter::class.java)
            .build()
    }
}

class SpaCsrfTokenRequestHandler : CsrfTokenRequestAttributeHandler(){
    private val delegate:CsrfTokenRequestHandler = CsrfTokenRequestAttributeHandler()

    override fun handle(
        req: HttpServletRequest,
        res: HttpServletResponse,
        t: Supplier<CsrfToken>
    ) {
        delegate.handle(req, res, t)
    }

    override fun resolveCsrfTokenValue(request: HttpServletRequest, csrfToken: CsrfToken): String? {
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
        val csrfToken = request.getAttribute("_csrf") as CsrfToken
        //render the token value to a cookie by causing the deferred token to be load
        csrfToken.token
        filterChain.doFilter(request,response)
    }
}