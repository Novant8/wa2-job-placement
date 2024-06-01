package it.polito.wa2.g07.communicationmanager.controllers

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.springframework.http.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ProblemDetailsHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(UnrecognizedPropertyException::class)
    fun handleUnrecognizedProperty (e: UnrecognizedPropertyException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,e.message!!)

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