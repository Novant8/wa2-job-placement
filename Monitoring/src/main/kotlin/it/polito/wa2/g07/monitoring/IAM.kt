package it.polito.wa2.g07.monitoring

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(private val authRecorderReposiytory: AuthRecorderReposiytory) {
        @KafkaListener(topics = ["IAM-LOGIN"], groupId = "group1",containerFactory = "kafkaListenerContainerFactory")
       // fun loginListener(message: String) {
        fun loginListener(@Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long, message: AuthDTO ) {
                println("Consumed message ${message.toString()} generated at $ts")



                var auth = authRecorderReposiytory.findById(message.userId)
                if (auth == null) {
                        println("Non esiste")
                        authRecorderReposiytory.save(AuthRecorder(message.userId,message.name,ts,0))
                }else{
                        println("Esiste")
                        auth.lastLoginTime = ts
                        authRecorderReposiytory.save(auth)
                }

                //val login = AuthRecorder("A","b",ts,0)
                //println(authRecorderReposiytory.save(login))
        }
        @KafkaListener(topics = ["IAM-LOGOUT"], groupId = "group1",containerFactory = "kafkaListenerContainerFactory")
        fun logoutListener(message: AuthDTO,@Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long) {
                println("Consumed message: $message")
                var auth = authRecorderReposiytory.findById(message.userId)
                if (auth == null) {
                        println("Non esiste")
                        authRecorderReposiytory.save(AuthRecorder(message.userId,message.name,ts,0))
                }else{
                        println("Esiste")
                        auth.totalTime += ts- auth.lastLoginTime!!
                        authRecorderReposiytory.save(auth)

                }

        }
}
data class AuthDTO(
        @JsonProperty("userId") val userId: String,
        @JsonProperty("username") val name: String?,
)
@Entity
public class AuthRecorder(
        @Id
        var id: String? = null,
        var username: String? = null,
        var lastLoginTime: Long? = null,
        var totalTime : Long = 0
){

}

@Repository
interface AuthRecorderReposiytory :JpaRepository<AuthRecorder, Long> {
        abstract fun findById(userId: String): AuthRecorder?

}