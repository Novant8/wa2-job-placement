package it.polito.wa2.g07.crm.controllers.project

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.services.project.JobProposalService
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.web.bind.annotation.*

@RestController
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/API/jobProposals")
class JobProposalController(private val jobProposalService: JobProposalService) {

    @Operation(summary = "Add new Job Proposal")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The Job Proposal was successfully created"),

        ApiResponse(
            responseCode = "404",
            description = "The job offer was not found or the Professional was not found or the Customer was not found ",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])

    @PreAuthorize("hasAnyRole('operator', 'manager' )")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{customerId}/{professionalId}/{jobOfferId}")
    fun createJobProposal(@PathVariable("customerId") customerId: Long, @PathVariable("professionalId") professionalId: Long, @PathVariable("jobOfferId") jobOfferId:Long  ): JobProposalDTO {
        return jobProposalService.createJobProposal(customerId, professionalId,jobOfferId)
    }

    @Operation(summary = "Retrieve a specific job proposal information")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The job proposal was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/{jobProposalId}")
    fun getJobsOfferSpecific(@PathVariable("jobProposalId") idProposal:Long) : JobProposalDTO{
        return jobProposalService.searchJobProposalById(idProposal)
    }


    @Operation(summary = "Retrieve a specific job proposal information by ProfessionalId and JobOfferId")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The job proposal was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/{jobOfferId}/{professionalId}")
    fun getJobsOfferByProfessionalAndOffer(
        @PathVariable("jobOfferId") idOffer:Long,
        @PathVariable("professionalId") idProfessional:Long)
    : JobProposalDTO{
        return jobProposalService.searchJobProposalByJobOfferAndProfessional(idProfessional, idOffer)
    }
}