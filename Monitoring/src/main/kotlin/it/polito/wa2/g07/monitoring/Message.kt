package it.polito.wa2.g07.monitoring

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

class Message {
}

@Service
class KafkaConsumerMessage(private val messageMonitoring: MessageMonitoringRepository) {
    @KafkaListener(topics = ["MESSAGE"], groupId = "group1",containerFactory = "kafkaMessageListenerContainerFactory")
    // fun loginListener(message: String) {
    fun messageListener( message: MessageMonitoringDTO ) {
        println("Consumed message ${message.toString()} ")

        var msg = messageMonitoring.findById(message.id)
        if (msg.isEmpty){
            messageMonitoring.save(MessageMonitoring(message.id,message.channel,message.priority,message.creationTimestamp,message.status,message.statusTimestamp))
        }else{
            msg.get().status =message.status
            msg.get().statusTimestamp =message.statusTimestamp
            msg.get().priority = message.priority
            messageMonitoring.save(msg.get())
        }

    }

}
data class MessageMonitoringDTO(
    @JsonProperty("id") var id: String,
    @JsonProperty("channel") var channel: String?,
    @JsonProperty("priority") var priority: Int?,
    @JsonProperty("creationTimestamp") var creationTimestamp: LocalDateTime?,
    @JsonProperty("status") var status: String,
    @JsonProperty("statusTimestamp") var statusTimestamp: LocalDateTime?,
)
@Entity
public class MessageMonitoring(
    @Id
    var idMessage: String?= null,
    var channel: String?= null,
    var priority: Int?= 0,
    var creationTimestamp: LocalDateTime?= null,
    var status: String?= null,
    var statusTimestamp: LocalDateTime?= null,
){


}

@Repository
interface MessageMonitoringRepository : JpaRepository<MessageMonitoring, String> {

}