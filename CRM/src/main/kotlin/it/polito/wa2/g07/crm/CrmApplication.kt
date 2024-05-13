package it.polito.wa2.g07.crm

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(
	info = Info(
		title = "CRM API",
		version = "1.0",
		description = "This tool acts as a centralized platform to manage and enhance interactions with candidates, clients, and other stakeholders involved in the hiring process by providing a centralized hub for managing candidates, job offers, and client relationships. It helps recruiters and HR professionals to work more effectively, make data-driven decisions, and ultimately improve the overall quality of placements."
	)
)
class CrmApplication

fun main(args: Array<String>) {
	runApplication<CrmApplication>(*args)
}
