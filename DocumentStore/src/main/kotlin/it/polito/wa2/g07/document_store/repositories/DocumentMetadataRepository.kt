package it.polito.wa2.g07.document_store.repositories


import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@EnableJpaRepositories
@Repository
interface DocumentMetadataRepository:JpaRepository<DocumentMetadata,Long> {

    @Query("""
        SELECT count(dm) > 0 FROM DocumentMetadata dm
        WHERE lower(dm.name) = lower(:name)
        AND (dm.documentHistory.id, dm.creationTimestamp) IN
        (
            SELECT dm1.documentHistory.id, max(dm1.creationTimestamp)
            FROM DocumentMetadata dm1
            GROUP BY dm1.documentHistory
        )
    """)
    fun existsByNameIgnoreCase(@Param("name") name: String): Boolean

    @Query("""
        SELECT count(dm) > 0 FROM DocumentMetadata dm
        WHERE lower(dm.name) = lower(:name)
        AND dm.documentHistory.id <> :historyId
        AND (dm.documentHistory.id, dm.creationTimestamp) IN
        (
            SELECT dm1.documentHistory.id, max(dm1.creationTimestamp)
            FROM DocumentMetadata dm1
            GROUP BY dm1.documentHistory
        )
    """)
    fun existsByNameIgnoreCaseAndDocumentHistoryIdNot(@Param("name") name: String, @Param("historyId") historyId: Long): Boolean

    fun existsByDocumentHistoryIdAndMetadataID(historyId: Long, metadataId: Long): Boolean

    fun findTopByDocumentHistoryIdOrderByCreationTimestampDesc(documentHistoryId: Long): Optional<DocumentMetadata>

    @Query("""
        SELECT dm FROM DocumentMetadata dm
        WHERE (dm.documentHistory.id, dm.creationTimestamp) IN
        (
            SELECT dm1.documentHistory.id, max(dm1.creationTimestamp)
            FROM DocumentMetadata dm1
            GROUP BY dm1.documentHistory
        )
    """)
    fun findAllMostRecentMetadataFromHistory(pageable: Pageable): Page<DocumentMetadata>

    fun findByDocumentHistoryIdAndMetadataID(historyId: Long, metadataID: Long): Optional<DocumentMetadata>

}