package it.polito.wa2.g07.crm.controllers.lab03

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(JobOfferController::class)
class JobOfferControllerTest(@Autowired val mockMvc: MockMvc) {

}