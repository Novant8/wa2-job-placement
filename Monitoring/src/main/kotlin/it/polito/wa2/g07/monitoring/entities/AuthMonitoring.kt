package it.polito.wa2.g07.monitoring.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
public class AuthMonitoring(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,
    var username: String? = null,
    var loginTime: Long? = null
){

}