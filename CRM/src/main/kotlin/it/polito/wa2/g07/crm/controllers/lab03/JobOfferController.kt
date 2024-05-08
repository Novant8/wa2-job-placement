package it.polito.wa2.g07.crm.controllers.lab03

import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.services.lab03.JobOfferService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/joboffers")
class JobOfferController(private val jobOfferService: JobOfferService) {


        @GetMapping("/")
        fun getJobsOffer(filterDTO: JobOfferFilterDTO, pageable: Pageable) : Page<JobOfferReducedDTO>{
            return jobOfferService.searchJobOffer(filterDTO,pageable)

        }
        @GetMapping("/{joboffer_id}")
        fun getJobsOfferSpecific(@PathVariable("joboffer_id") idOffer:Long) : JobOfferDTO{
            return jobOfferService.searchJobOfferById(idOffer)
        }


        @PostMapping("/{joboffer_id}/")
        fun updateJobOfferStatus(){
            TODO("Not yet implemented")
           // change the status of a specific Job offer. This endpoint must receive the target status and if necessary a note and reference to a professional
        }

        @GetMapping("/{joboffer_id}/value")
        fun getJobOfferValid(@PathVariable("joboffer_id") idOffer:Long) : Map<String,Double?>{

               return mapOf<String,Double?>("value" to jobOfferService.getJobOfferValue(idOffer))

        }
}