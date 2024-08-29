package it.polito.wa2.g07.monitoring.repositories


import it.polito.wa2.g07.monitoring.entities.DocumentMetadataMonitoring
import it.polito.wa2.g07.monitoring.entities.JobOfferMonitoring
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository

@EnableJpaRepositories
@Repository
interface JobOfferMonitoringRepository:JpaRepository<JobOfferMonitoring,Long> {

}