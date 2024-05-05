package it.polito.wa2.g07.crm.controllers.lab03

import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalReducedDTO
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import jakarta.validation.Valid
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
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/API/professionals")
class ProfessionalController(
    private val professionalService: ProfessionalService
) {

   /* The web application must allow the creation of a new customer or
    professional, search existing ones, update their properties, and add notes on
    them.*/
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/","")
    fun createProfessional(@Valid @RequestBody professional: Professional) /*: ProfessionalDTO*/{
        TODO()
        //TO DO: professionalService.create(professional)
    }

    @PutMapping("{professionalId}/notes")
    fun updateProfessionalNotes(@PathVariable professionalId:Long,
                                   @RequestBody notes: String
                                    )/*: ProfessionalDTO?*/
    {
        TODO()
    }

    @GetMapping("/", "")
    fun getProfessionals(filterDTO: ProfessionalFilterDTO, pageable: Pageable) : Page<ProfessionalReducedDTO> {
        return professionalService.searchProfessionals(filterDTO, pageable)
    }

    @GetMapping("/{professionalId}", "/{professionalId}/")
    fun getProfessionalById(@PathVariable professionalId: Long): ProfessionalDTO {
        return professionalService.getProfessionalById(professionalId)
    }

    @PutMapping("/{professionalId}/skill")
    fun updateSkill(@PathVariable("professionalId") professionalId: Long, @RequestBody skills :Map<String, List<String>>){
        TODO()
    }

    @PutMapping("/{professionalId}/location")
    fun updateLocation(@PathVariable("professionalId") professionalId: Long, @RequestBody location :Map<String,String >){
        TODO()
    }

    @PutMapping("/{professionalId}/rate")
    fun updateDailyRate(@PathVariable("professionalId") professionalId: Long, @RequestBody rate :Map<String,Double >){
        TODO()
    }
}