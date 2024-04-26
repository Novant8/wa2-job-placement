package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.exceptions.ContactNotFoundException
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.MissingFieldException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.exceptions.MessageNotFoundException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@RestControllerAdvice
class ProblemDetailsHandler: ResponseEntityExceptionHandler() {
    @ExceptionHandler(MissingFieldException::class)
    fun handleMissingFields(e:MissingFieldException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message !!)

    @ExceptionHandler(InvalidParamsException::class)
    fun handleDuplicateDocument(e: InvalidParamsException) =
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.message!!)

    @ExceptionHandler(ContactNotFoundException::class)
    fun handleContactNotFound (e: ContactNotFoundException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.message!!)

    @ExceptionHandler(MessageNotFoundException::class)
    fun handleContactNotFound (e: MessageNotFoundException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.message!!)

    @ExceptionHandler(DuplicateAddressException::class)
    fun handleContactNotFound (e: DuplicateAddressException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_MODIFIED,e.message!!)
}