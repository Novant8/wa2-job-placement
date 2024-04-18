package it.polito.wa2.g07.crm.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class MissingFieldException (message:String):RuntimeException(message)