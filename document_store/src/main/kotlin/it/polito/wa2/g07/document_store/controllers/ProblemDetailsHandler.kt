package it.polito.wa2.g07.document_store.controllers

import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.exceptions.InvalidBodyException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ProblemDetailsHandler :ResponseEntityExceptionHandler(){

    @ExceptionHandler(DocumentNotFoundException::class)
    fun handleDocumentNotFound(e: DocumentNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.message !!)

    @ExceptionHandler(DuplicateDocumentException::class)
    fun handleDuplicateDocument(e: DuplicateDocumentException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.message !!)

    @ExceptionHandler(InvalidBodyException::class)
    fun handleDuplicateDocument(e: InvalidBodyException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.message !!)

}