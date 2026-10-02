package io.github.assassinpunk.inventory.domain

import jakarta.persistence.*

@Entity
@Table(name = "stores")
class Store(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 6)
    var pincode: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)