package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
public open class  Address {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    open var id: Long=0

}