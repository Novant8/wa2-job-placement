package it.polito.wa2.g07.crm.controllers.lab02


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.entities.lab02.MessageStatus
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab02.MessageService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.*

@Tag(name = "Messages", description = "Create, search, track and update the status of messages")
@RestController
@RequestMapping("API/messages")
class MessageController (private val messageService: MessageService
                        ) {

    @Operation(summary = "List all messages, optionally filtering by status, with pagination and sorting")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = "An invalid filter was provided",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("","/")
    fun getMessages(
        @RequestParam("filterBy", required = false)
        @Parameter(
            description = "List of message statuses. Results will show all messages that are currently in any of the given statuses.",
            array = ArraySchema(schema = Schema(implementation = MessageStatus::class))
        )
        filterByStr: List<String>? = null,

        @ParameterObject pageable: Pageable
    ):Page<ReducedMessageDTO>{

        if (filterByStr == null  ){  return messageService.getMessages(null,pageable = pageable)}
        if (filterByStr.isEmpty() ) {  throw InvalidParamsException("'$filterByStr' is not a valid filter. Possible filters: ${MessageStatus.entries}.") }

        val filterBy = try {
            filterByStr.map{ MessageStatus.valueOf(it.uppercase())}
        } catch (e: IllegalArgumentException) {
                throw InvalidParamsException("'$filterByStr' is not a valid filter. Possible filters: ${MessageStatus.entries}.")
        }
        return messageService.getMessages(filterBy=filterBy, pageable = pageable)


    }

    @Operation(summary = "Create a new message")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The message was successfully created"
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid message data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("","/")
    fun createNewMessage(@RequestBody @Valid  msg: MessageCreateDTO): MessageDTO? {
       // sender, channel, subject, body
        val channel = try {
            MessageChannel.valueOf(msg.channel.uppercase())
        } catch (e : IllegalArgumentException) {
            throw InvalidParamsException("'${msg.channel}' is not a valid address channel. Possible channels: ${MessageChannel.entries}.")
        }
        if (!msg.sender.compatibleChannels.contains(channel)){
            throw InvalidParamsException("Sender's fields are incompatible with channel type")
        }
         return messageService.createMessage(msg)
    }

    @Operation(summary = "Change the status of a specific message")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The message status was successfully updated"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid status data was supplied",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "The message was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id_message}","/{id_message}/")
    fun updateMessageState(@PathVariable("id_message") idMessage: Long,
                            @RequestBody   event: MessageEventDTO
    ): MessageEventDTO?{
        return messageService.updateStatus(idMessage, event)
    }

    @Operation(summary = "Retrieve a specific message's data")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The message was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/{messageId}", "/{messageId}/")
    fun getMessageById(@PathVariable("messageId") messageId: Long) : MessageDTO? {
        return  messageService.getMessage(messageId)

    }

    @Operation(summary = "Retrieve the list of state changes, with their comments, for a specific message")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The message was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/{messageId}/history","/{messageId}/history/")
    fun retrieveMessageStateChanges(@PathVariable("messageId") messageId: Long,
                                    @ParameterObject pageable: Pageable): Page<MessageEventDTO>
    {
        return messageService.getHistory(messageId,pageable)
    }

    @Operation(summary = "Modify the priority of a message")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "200",
            description = "The priority has been successfully modified"
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid priority was given",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PutMapping("/{messageId}/priority","/{messageId}/priority/")
    fun modifyMessagePriority(
        @PathVariable("messageId") messageId: Long,
        @Valid @RequestBody priorityDTO: PriorityDTO
    ): MessageDTO?
    {
        return messageService.changePriority(messageId,priorityDTO.priority)
    }

}

