package it.polito.wa2.g07.crm.controllers.project

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferUpdateDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.services.project.JobProposalService
import org.keycloak.admin.client.Keycloak
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/API/jobProposals")
class JobProposalController(private val jobProposalService: JobProposalService) {

    private lateinit var restTemplate: RestTemplate

    @Value("\${job-placement.document-store-url}")
    lateinit var documentStorePath: String

    @Autowired
    fun setRestTemplate(restTemplateBuilder: RestTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build()
    }

    @Autowired
    private lateinit var keycloak: Keycloak

    @Operation(summary = "Add new Job Proposal")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The Job Proposal was successfully created"),

        ApiResponse(
            responseCode = "404",
            description = "The job proposal was not found ",
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
        return jobProposalService.searchJobProposalByJobOfferAndProfessional(idOffer,idProfessional)
    }

    @Operation(summary = "Update Customer confirm")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The Job Proposal was successfully updated"),

        ApiResponse(
            responseCode = "404",
            description = "The job proposal was not found  ",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])

    @PreAuthorize("hasAnyRole('operator', 'manager','customer' )")
    @PutMapping("/{proposalId}/{customerId}")
    fun customerConfirmDecline(@PathVariable("proposalId") proposalId: Long, @PathVariable("customerId") customerId : Long ,@RequestBody customerConfirm: Boolean): JobProposalDTO {
        return jobProposalService.customerConfirmDecline(proposalId, customerId, customerConfirm)
    }

    @Operation(summary = "Update JobProposal status after professional decision")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The Job Proposal was successfully updated"),

        ApiResponse(
            responseCode = "404",
            description = "The job proposal was not found  ",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])

    @PreAuthorize("hasAnyRole('operator', 'manager','professional' )")
    @PutMapping("professional/{proposalId}/{professionalId}")
    fun professionalConfirmDecline(@PathVariable("proposalId") proposalId: Long, @PathVariable("professionalId") professionalId : Long ,@RequestBody professionalConfirm: Boolean): JobProposalDTO {
        return jobProposalService.professionalConfirmDecline(proposalId, professionalId, professionalConfirm)
    }

    @Operation(summary = "Load a New Document for the Job Proposal ")
    @PreAuthorize("hasAnyRole('customer', 'professional' )")
    @PutMapping("{proposalId}/document")
    fun loadDocument (@PathVariable("proposalId") proposalId: Long,@RequestBody documentId: Long?): JobProposalDTO{
        return jobProposalService.loadDocument(proposalId,documentId)
    }

    @Operation(summary = "Load a Professional Signed Document for the Job Proposal ")
    @PreAuthorize("hasAnyRole('customer', 'professional' )")
    @PutMapping("{proposalId}/signedDocument")
    fun loadSignedDocument (@PathVariable("proposalId") proposalId: Long,@RequestBody documentId: Long?): JobProposalDTO{
        return jobProposalService.loadSignedDocument(proposalId,documentId)
    }

    @Operation(summary = "Retrieve a job proposal contract's history")
    @PreAuthorize("hasAnyRole('operator', 'manager') or @jobProposalServiceImpl.userCanAccessContract(authentication.name, #proposalId)")
    @GetMapping("{proposalId}/document/history")
    fun getDocumentHistory(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable proposalId: Long,
    ): ResponseEntity<*> {
        val documentId = jobProposalService.searchJobProposalById(proposalId).documentId
            ?: throw EntityNotFoundException("The job proposal does not have a contract assigned")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/history",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }

    @Operation(summary = "Retrieve a job proposal contract's latest version's raw data")
    @PreAuthorize("hasAnyRole('operator', 'manager') or @jobProposalServiceImpl.userCanAccessContract(authentication.name, #proposalId)")
    @GetMapping("{proposalId}/document/data")
    fun getDocumentData(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable proposalId: Long,
    ): ResponseEntity<*> {
        val documentId = jobProposalService.searchJobProposalById(proposalId).documentId
            ?: throw EntityNotFoundException("The job proposal does not have a contract assigned")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/data",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }

    @Operation(summary = "Retrieve a job proposal contract's specific version's raw data")
    @PreAuthorize("hasAnyRole('operator', 'manager') or @jobProposalServiceImpl.userCanAccessContract(authentication.name, #proposalId)")
    @GetMapping("{proposalId}/document/version/{versionId}/data")
    fun getDocumentVersionData(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable proposalId: Long,
        @PathVariable versionId: Long
    ): ResponseEntity<*> {
        val documentId = jobProposalService.searchJobProposalById(proposalId).documentId
            ?: throw EntityNotFoundException("The job proposal does not have a contract assigned")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/version/$versionId/data",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }

    @Operation(summary = "Retrieve a job proposal signed contract's history")
    @PreAuthorize("hasAnyRole('operator', 'manager') or @jobProposalServiceImpl.userCanAccessContract(authentication.name, #proposalId)")
    @GetMapping("{proposalId}/document/signed/history")
    fun getSignedDocumentHistory(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable proposalId: Long,
    ): ResponseEntity<*> {
        val documentId = jobProposalService.searchJobProposalById(proposalId).professionalSignedContract
            ?: throw EntityNotFoundException("The job proposal does not have a signed contract assigned")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/history",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }

    @Operation(summary = "Retrieve a signed job proposal contract's latest version's raw data")
    @PreAuthorize("hasAnyRole('operator', 'manager') or @jobProposalServiceImpl.userCanAccessContract(authentication.name, #proposalId)")
    @GetMapping("{proposalId}/document/signed/data")
    fun getSignedDocumentData(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable proposalId: Long,
    ): ResponseEntity<*> {
        val documentId = jobProposalService.searchJobProposalById(proposalId).professionalSignedContract
            ?: throw EntityNotFoundException("The job proposal does not have a signed contract assigned")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/data",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }

    @Operation(summary = "Retrieve a signed job proposal contract's specific version's raw data")
    @PreAuthorize("hasAnyRole('operator', 'manager') or @jobProposalServiceImpl.userCanAccessContract(authentication.name, #proposalId)")
    @GetMapping("{proposalId}/document/signed/version/{versionId}/data")
    fun getSignedDocumentVersionData(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable proposalId: Long,
        @PathVariable versionId: Long
    ): ResponseEntity<*> {
        val documentId = jobProposalService.searchJobProposalById(proposalId).professionalSignedContract
            ?: throw EntityNotFoundException("The job proposal does not have a signed contract assigned")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/version/$versionId/data",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }
}