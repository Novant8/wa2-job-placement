package it.polito.wa2.g07.crm.services.project

interface KeycloakUserService {

    fun setUserAsCustomer(userId: String)
    fun setUserAsProfessional(userId: String)

    fun changeUserName(userId: String, name: String)
    fun changeUserSurname(userId: String, surname: String)
    fun changeUserEmail(userId: String, email: String)

}