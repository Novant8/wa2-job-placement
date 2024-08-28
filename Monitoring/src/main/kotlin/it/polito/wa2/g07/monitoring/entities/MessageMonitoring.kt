package it.polito.wa2.g07.monitoring.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

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