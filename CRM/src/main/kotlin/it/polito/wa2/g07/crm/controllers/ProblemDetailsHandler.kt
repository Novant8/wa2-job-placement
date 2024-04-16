package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@RestControllerAdvice
class ProblemDetailsHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(InvalidParamsException::class)
    fun handleDuplicateDocument(e: InvalidParamsException) =
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.message!!)

}