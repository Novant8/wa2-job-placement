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
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Pageable
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.*

@Tag(name = "4. Professionals", description = "Create, search and update professionals' information")
@RestController
@RequestMapping("/API/professionals")
class ProfessionalController (private val professionalService: ProfessionalService,private val contactService: ContactService) {

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
    //Modify Professional notes
    @PutMapping("{professionalId}/notes")
    fun updateProfessionalNotes(@PathVariable professionalId:Long,
                                   @RequestBody notes: NotesDTO
                                    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalNotes(professionalId, notes.notes)
    }
    //Modify professional location
    @PutMapping("{professionalId}/location")
    fun updateProfessionalLocation(@PathVariable professionalId:Long,
                                @RequestBody location :Map<String, String>
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalLocation(professionalId, location["location"]!!)
    }

    @PutMapping("{professionalId}/skills")
    fun updateProfessionalSkills(@PathVariable professionalId:Long,
                                   @RequestBody skills :Map<String, Set<String>>
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalSkills(professionalId, skills["location"]!!)
    }

    @PutMapping("{professionalId}/employmentState")
    fun updateProfessionalEmploymentState(@PathVariable professionalId:Long,
                                 @RequestBody employmentState :Map<String, EmploymentState>
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalEmploymentState(professionalId, employmentState["employmentState"]!!)
    }

    @PutMapping("{professionalId}/dailyRate")
    fun updateProfessionalDailyRate(@PathVariable professionalId:Long,
                                          @RequestBody dailyRate :Map<String, Double>
    ): ProfessionalDTO?
    {
        return professionalService.postProfessionalDailyRate(professionalId, dailyRate["dailyRate"]!!)
    }

    @PutMapping("/{professionalId}/email/{emailId}","/{professionalId}/email/{emailId}")
    fun editProfessionalEmail(@PathVariable("professionalId") professionalId :Long, @PathVariable("emailId") emailId : Long,
                              @Valid @RequestBody emailDTO: EmailDTO
    ): ProfessionalDTO{
        val professional = professionalService.getProfessionalById(professionalId)
        val contactId =  professional.contactInfo.id
        val contactDTO= contactService.updateAddress(contactId,emailId,emailDTO)

        return ProfessionalDTO(professional.id, contactDTO,professional.location,professional.skills,professional.dailyRate,professional.employmentState,professional.notes)

    }

    @PutMapping("/{professionalId}/telephone/{telephoneId}","/{professionalId}/telephone/{telephoneId}")
    fun editProfessionalTelephone(@PathVariable("professionalId") professionalId :Long, @PathVariable("telephoneId") telephoneId : Long,
                              @Valid @RequestBody telephoneDTO: TelephoneDTO
    ): ProfessionalDTO{
        val professional = professionalService.getProfessionalById(professionalId)
        val contactId =  professional.contactInfo.id
        val contactDTO= contactService.updateAddress(contactId,telephoneId,telephoneDTO)

        return ProfessionalDTO(professional.id, contactDTO,professional.location,professional.skills,professional.dailyRate,professional.employmentState,professional.notes)

    }

    @PutMapping("/{professionalId}/dwelling/{dwellingId}","/{professionalId}/dwelling/{dwellingId}")
    fun editProfessionalDwelling(@PathVariable("professionalId") professionalId :Long, @PathVariable("dwellingId") dwellingId : Long,
                                  @Valid @RequestBody dwellingDTO: DwellingDTO
    ): ProfessionalDTO{
        val professional = professionalService.getProfessionalById(professionalId)
        val contactId =  professional.contactInfo.id
        val contactDTO= contactService.updateAddress(contactId,dwellingId,dwellingDTO)

        return ProfessionalDTO(professional.id, contactDTO,professional.location,professional.skills,professional.dailyRate,professional.employmentState,professional.notes)

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

    @Operation(summary = "Update a professional's information")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The professional was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid professional data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PutMapping("/{professionalId}")
    fun updateProfessional(
        @PathVariable("professionalId") professionalId: Long,
        @RequestBody updateDTO: UpdateProfessionalDTO
    ): ProfessionalDTO? {
        TODO()
    }
}