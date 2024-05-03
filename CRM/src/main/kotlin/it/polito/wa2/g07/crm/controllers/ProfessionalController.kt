package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.entities.Professional
import jakarta.validation.Valid
import org.springframework.beans.factory.parsing.Location
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/API/professional")
class ProfessionalController {

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





}