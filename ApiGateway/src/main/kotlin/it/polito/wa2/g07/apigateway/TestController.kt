package it.polito.wa2.g07.apigateway

import org.springframework.security.core.Authentication
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

}