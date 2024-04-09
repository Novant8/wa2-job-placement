package it.polito.wa2.g07.document_store.repositories


import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository

@EnableJpaRepositories
@Repository
interface DocumentMetadataRepository:JpaRepository<DocumentMetadata,Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
    fun existsByNameIgnoreCaseAndMetadataIDNot(name: String, metadataId: Long): Boolean
}