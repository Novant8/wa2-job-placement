package it.polito.wa2.g07.crm.controllers.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import it.polito.wa2.g07.crm.dtos.lab02.DwellingDTO
import it.polito.wa2.g07.crm.dtos.lab02.EmailDTO
import it.polito.wa2.g07.crm.dtos.lab02.NotesDTO
import it.polito.wa2.g07.crm.dtos.lab02.TelephoneDTO
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import jakarta.validation.Valid
import org.keycloak.admin.client.Keycloak
import org.springdoc.core.annotations.ParameterObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Pageable
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Tag(name = "4. Professionals", description = "Create, search and update professionals' information")
@RestController
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/API/professionals")
class ProfessionalController (private val professionalService: ProfessionalService,private val contactService: ContactService) {

    private lateinit var restTemplate: RestTemplate

    @Value("\${job-placement.document-store-url}")
    lateinit var documentStorePath: String

    @Autowired
    fun setRestTemplate(restTemplateBuilder: RestTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build()
    }

    @Autowired
    private lateinit var keycloak: Keycloak

    @Operation(summary = "Create a new professional")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The professional was successfully created"
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid professional data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    @PostMapping("/","")
    fun createProfessional(@Valid @RequestBody professional: CreateProfessionalDTO) : ProfessionalDTO{
        return  professionalService.createProfessional(professional)
    }

    @Operation(summary = "Add or update an existing professional's notes")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    @PutMapping("{professionalId}/notes")
    fun updateProfessionalNotes(@PathVariable professionalId:Long,
                                   @RequestBody notes: NotesDTO
                                    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalNotes(professionalId, notes.notes)
    }

    @Operation(summary = "Update an existing professional's location")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userIsProfessionalWithId(authentication.name, #professionalId)")
    @PutMapping("{professionalId}/location")
    fun updateProfessionalLocation(
        @PathVariable professionalId:Long,
        @RequestBody locationDTO: LocationDTO
    ): ProfessionalDTO
    {
        return professionalService.postProfessionalLocation(professionalId, locationDTO.location)
    }

    @Operation(summary = "Update an existing professional's skills")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    // Authorize only if the authenticated user is an operator/manager, or if they are trying to modify their own attributes.
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userIsProfessionalWithId(authentication.name, #professionalId)")
    @PutMapping("{professionalId}/skills")
    fun updateProfessionalSkills(
        @PathVariable professionalId:Long,
        @RequestBody skillsDTO: SkillsDTO
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalSkills(professionalId, skillsDTO.skills)
    }

    @Operation(summary = "Update an existing professional's employment state")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    @PutMapping("{professionalId}/employmentState")
    fun updateProfessionalEmploymentState(
        @PathVariable professionalId:Long,
        @RequestBody employmentStateDTO: EmploymentStateDTO
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalEmploymentState(professionalId, employmentStateDTO.employmentState)
    }

    @Operation(summary = "Update an existing professional's daily rate")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    // Authorize only if the authenticated user is an operator/manager, or if they are trying to modify their own attributes.
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userIsProfessionalWithId(authentication.name, #professionalId)")
    @PutMapping("{professionalId}/dailyRate")
    fun updateProfessionalDailyRate(@PathVariable professionalId:Long,
                                          @RequestBody dailyRateDTO: DailyRateDTO
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalDailyRate(professionalId, dailyRateDTO.dailyRate)
    }

    @Operation(summary = "Update an existing professional's CV document")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    // Authorize only if the authenticated user is an operator/manager, or if they are trying to modify their own attributes.
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userIsProfessionalWithId(authentication.name, #professionalId)")
    @PutMapping("{professionalId}/cvDocument")
    fun updateProfessionalCvDocument(@PathVariable professionalId:Long,
                                    @RequestBody cvDocumentDTO: CvDocumentDTO
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalCvDocument(professionalId, cvDocumentDTO)
    }

    @Operation(summary = "Update an existing professional's e-mail")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    @PutMapping("/{professionalId}/email/{emailId}","/{professionalId}/email/{emailId}")
    fun editProfessionalEmail(@PathVariable("professionalId") professionalId :Long, @PathVariable("emailId") emailId : Long,
                              @Valid @RequestBody emailDTO: EmailDTO
    ): ProfessionalDTO{
        val professional = professionalService.getProfessionalById(professionalId)
        val contactId =  professional.contactInfo.id
        val contactDTO= contactService.updateAddress(contactId,emailId,emailDTO)

        return professional.copy(contactInfo=contactDTO)
    }

    @Operation(summary = "Update an existing professional's phone number")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    @PutMapping("/{professionalId}/telephone/{telephoneId}","/{professionalId}/telephone/{telephoneId}")
    fun editProfessionalTelephone(@PathVariable("professionalId") professionalId :Long, @PathVariable("telephoneId") telephoneId : Long,
                              @Valid @RequestBody telephoneDTO: TelephoneDTO
    ): ProfessionalDTO{
        val professional = professionalService.getProfessionalById(professionalId)
        val contactId =  professional.contactInfo.id
        val contactDTO= contactService.updateAddress(contactId,telephoneId,telephoneDTO)

        return professional.copy(contactInfo=contactDTO)

    }

    @Operation(summary = "Update an existing professional's home/dwelling address")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    @PutMapping("/{professionalId}/address/{dwellingId}","/{professionalId}/address/{dwellingId}")
    fun editProfessionalDwelling(@PathVariable("professionalId") professionalId :Long, @PathVariable("dwellingId") dwellingId : Long,
                                  @Valid @RequestBody dwellingDTO: DwellingDTO
    ): ProfessionalDTO{
        val professional = professionalService.getProfessionalById(professionalId)
        val contactId =  professional.contactInfo.id
        val contactDTO= contactService.updateAddress(contactId,dwellingId,dwellingDTO)

        return professional.copy(contactInfo=contactDTO)
    }

    @Operation(summary = "List all professionals that match the given filters, with paging and sorting")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = "Something in the given filter is invalid",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/", "")
    fun getProfessionals(
        filterDTO: ProfessionalFilterDTO,
        @ParameterObject pageable: Pageable
    ) : Page<ProfessionalReducedDTO> {
        return professionalService.searchProfessionals(filterDTO, pageable)
    }

    @Operation(summary = "Retrieve professional information related to the current user")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The user is not associated to any professional",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/user/me", "/user/me/")
    fun getProfessionalMe(authentication: Authentication): ProfessionalDTO {
        return professionalService.getProfessionalFromUserId(authentication.name)
    }

    @Operation(summary = "Retrieve a specific professional's information")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/{professionalId}", "/{professionalId}/")
    fun getProfessionalById(@PathVariable professionalId: Long): ProfessionalDTO {
        return professionalService.getProfessionalById(professionalId)
    }

    @Operation(summary = "Retrieve a specific professional's CV document's history")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found or has no CV",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userCanAccessProfessionalCv(authentication.name, #professionalId)")
    @GetMapping("/{professionalId}/cv/history")
    fun getProfessionalCVHistory(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable professionalId: Long
    ): ResponseEntity<*> {
        val documentId = professionalService.getProfessionalById(professionalId).cvDocument
            ?: throw EntityNotFoundException("The professional does not have a CV.")
        val processedHeaders = httpHeaders.apply {
            setBearerAuth(keycloak.tokenManager().accessTokenString)
        }
        println(processedHeaders["Authorization"])
        return restTemplate.exchange(
            "$documentStorePath/API/documents/$documentId/history",
            HttpMethod.GET,
            HttpEntity<ByteArray>(processedHeaders),
            ByteArray::class.java
        )
    }

    @Operation(summary = "Retrieve the content of a specific professional's CV document's latest version")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found or has no CV",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userCanAccessProfessionalCv(authentication.name, #professionalId)")
    @GetMapping("/{professionalId}/cv/data")
    fun getProfessionalCVData(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable professionalId: Long
    ): ResponseEntity<*> {
        val documentId = professionalService.getProfessionalById(professionalId).cvDocument
            ?: throw EntityNotFoundException("The professional does not have a CV.")
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

    @Operation(summary = "Retrieve the content of a specific professional's CV document's version")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found or has no CV",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PreAuthorize("hasAnyRole('operator', 'manager') or @professionalServiceImpl.userCanAccessProfessionalCv(authentication.name, #professionalId)")
    @GetMapping("/{professionalId}/cv/version/{versionId}/data")
    fun getProfessionalCVVersionData(
        @RequestHeader httpHeaders: HttpHeaders,
        @PathVariable professionalId: Long,
        @PathVariable versionId: Long,
    ): ResponseEntity<*> {
        val documentId = professionalService.getProfessionalById(professionalId).cvDocument
            ?: throw EntityNotFoundException("The professional does not have a CV.")
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