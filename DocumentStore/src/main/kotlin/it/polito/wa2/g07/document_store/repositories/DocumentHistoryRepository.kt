package it.polito.wa2.g07.document_store.repositories

import it.polito.wa2.g07.document_store.entities.DocumentHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentHistoryRepository : JpaRepository<DocumentHistory, Long>