package it.polito.wa2.g07.monitoring.repositories

import it.polito.wa2.g07.monitoring.entities.JobProposalMonitoring
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository

@EnableJpaRepositories
@Repository
interface JobProposalMonitoringRepository : JpaRepository<JobProposalMonitoring,Long>{
}