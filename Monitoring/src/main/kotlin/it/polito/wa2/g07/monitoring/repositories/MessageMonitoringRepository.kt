package it.polito.wa2.g07.monitoring.repositories

import it.polito.wa2.g07.monitoring.entities.MessageMonitoring
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageMonitoringRepository : JpaRepository<MessageMonitoring, String> {

}