package it.polito.wa2.g07.crm.repositories


import it.polito.wa2.g07.crm.entities.Message

import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository

@Repository
interface MessageRepository:JpaRepository<Message,Long> {


}