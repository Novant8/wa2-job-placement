package it.polito.wa2.g07.crm.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/API/joboffers")
class JobOfferController {


        @GetMapping("/getJobOffer")
        fun getJobsOffer(){
            TODO("Not yet implemented")
            //using request parameters allow for filtering
            //by customer, professional, and status.
        }


        @PostMapping("/{joboffer_id}/")
        fun updateJobOfferStatus(){
            TODO("Not yet implemented")
           // change the status of a specific Job offer. This endpoint must receive the target status and if necessary a note and reference to a professional
        }

        @GetMapping("/{joboffer_id}/value")
        fun getJobOfferValid(){
            TODO("Not yet implemented")
            //retrieve the value of a specific Job offer. Pay attention that value is confirmed only if a job offer is  bound to a professional.
        }
}