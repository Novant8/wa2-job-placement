package it.polito.wa2.g07.communicationmanager.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("API/emails")
class EmailController {

    @Operation(summary = "Send an e-mail")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "The e-mail was successfully sent"
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid customer data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PostMapping("", "/")
    fun sendEmail() {
        TODO()
    }

}