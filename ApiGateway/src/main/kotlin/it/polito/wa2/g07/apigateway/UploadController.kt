package it.polito.wa2.g07.apigateway

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/upload")
class UploadController {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var authorizedClientService: OAuth2AuthorizedClientService

    @Value("\${spring.cloud.gateway.mvc.routes[0].uri}")
    lateinit var servicePath: String

    fun getJwtToken(
        authentication: Authentication
    ): String {
        val clientToken = authentication as OAuth2AuthenticationToken
        val oauth2Client: OAuth2AuthorizedClient = authorizedClientService.loadAuthorizedClient(clientToken.authorizedClientRegistrationId, clientToken.name)
        return oauth2Client.accessToken.tokenValue
    }

    fun uploadOrUpdateDocument(
        httpMethod: HttpMethod,
        httpHeaders: HttpHeaders,
        file: ByteArray,
        authentication: Authentication,
        documentId: Long? = null
    ): ResponseEntity<ByteArray> {
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(getJwtToken(authentication))
            remove("Host")
        }
        return restTemplate.exchange(
            if(documentId == null) { "$servicePath/API/documents" } else { "$servicePath/API/documents/$documentId" },
            httpMethod,
            HttpEntity(file, processedHeaders),
            ByteArray::class.java
        )
    }

    @PostMapping("/document")
    fun uploadDocument(
        @RequestHeader httpHeaders: HttpHeaders,
        @RequestBody file: ByteArray,
        authentication: Authentication
    ): ResponseEntity<*> {
        return uploadOrUpdateDocument(
            HttpMethod.POST,
            httpHeaders,
            file,
            authentication
        )
    }

    @PutMapping("/document/{documentId}")
    fun updateDocument(
        @RequestHeader httpHeaders: HttpHeaders,
        @RequestBody file: ByteArray,
        authentication: Authentication,
        @PathVariable("documentId") documentId: Long
    ): ResponseEntity<*> {
        return uploadOrUpdateDocument(
            HttpMethod.PUT,
            httpHeaders,
            file,
            authentication,
            documentId
        )
    }

}