package it.polito.wa2.g07.monitoring.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
public class AuthMonitoring(
    @Id
    var id: String? = null,
    var username: String? = null,
    var lastLoginTime: Long? = null,
    var totalTime : Long = 0,
    var isLogged : Boolean=false
){

}