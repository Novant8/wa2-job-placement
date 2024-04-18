package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.Dwelling
import it.polito.wa2.g07.crm.entities.Email
import it.polito.wa2.g07.crm.entities.Message
import it.polito.wa2.g07.crm.entities.Telephone


data class ReducedMessageDTO (
        val id: Long,
        val subject: String,
        val sender:String,
        val channel:String
)

fun Message.toReducedDTO():ReducedMessageDTO{

       return  when (this.sender) {
                is Email ->{ return ReducedMessageDTO(this.messageID, this.subject, (this.sender as Email).email,"email" ) }
                is Dwelling ->{ return ReducedMessageDTO(this.messageID, this.subject, (this.sender as Dwelling).city,"dwelling" ) }
                is Telephone ->{ return ReducedMessageDTO(this.messageID, this.subject, (this.sender as Telephone).number,"telephone" ) }
                else-> { throw Exception("")  }
       }


}


