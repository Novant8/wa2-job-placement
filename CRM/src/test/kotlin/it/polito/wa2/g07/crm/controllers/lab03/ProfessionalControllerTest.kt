package it.polito.wa2.g07.crm.controllers.lab03

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc


@WebMvcTest(CustomerController::class)
class ProfessionalControllerTest(@Autowired val mockMvc: MockMvc) {

}