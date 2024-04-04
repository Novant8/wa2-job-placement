package it.polito.wa2.g07.document_store.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class InvalidBodyException(message: String) : RuntimeException(message)