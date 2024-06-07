package it.polito.wa2.g07.apigateway

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/", "")
    fun test(): Map<String,String> {
        return mapOf(
            "result" to "OK"
        )
    }

    @GetMapping("/authenticated")
    fun authenticated(authentication: Authentication?): Map<String,Any?> {
        return mapOf(
            "result" to "OK",
            "authentication" to authentication
        )
    }

    @GetMapping("/me")
    fun me (
        @CookieValue(name="XSRF-TOKEN", required = false)
        xsrf: String?,
        authentication: Authentication?
    ):Map<String,Any?>{
        val principal :OidcUser? = authentication?.principal as? OidcUser
        val name = principal?.givenName ?:""
        val surname = principal?.familyName ?: ""
        val role = principal?.authorities ?: ""
        return mapOf(
            "name" to name,
            "surname" to surname,
            "loginUrl" to "/oauth2/authorization/crmclient",
            "logoutUrl" to "/logout",
            "principal" to principal,
            "xsrfToken" to xsrf,
            "role" to role
        )
    }

}