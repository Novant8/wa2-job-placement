package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.exceptions.*
import org.springframework.web.bind.annotation.ExceptionHandler
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.exceptions.MessageNotFoundException
import org.springframework.http.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@RestControllerAdvice
class ProblemDetailsHandler: ResponseEntityExceptionHandler() {
    @ExceptionHandler(MissingFieldException::class)
    fun handleMissingFields(e:MissingFieldException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message !!)

    @ExceptionHandler(InvalidParamsException::class)
    fun handleDuplicateDocument(e: InvalidParamsException) =
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.message!!)

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleContactNotFound (e: EntityNotFoundException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.message!!)

    @ExceptionHandler(MessageNotFoundException::class)
    fun handleContactNotFound (e: MessageNotFoundException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.message!!)

    @ExceptionHandler(DuplicateAddressException::class)
    fun handleContactNotFound (e: DuplicateAddressException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.message!!)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "The request body contains invalid fields.")
        problemDetail.setProperty("fieldErrors", ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage })
        return ResponseEntity.unprocessableEntity().body(problemDetail)
    }
}