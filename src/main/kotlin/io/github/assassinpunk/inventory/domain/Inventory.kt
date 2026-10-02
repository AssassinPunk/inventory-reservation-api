package io.github.assassinpunk.inventory.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "inventory",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_inventory_product_store", columnNames = ["product_id", "store_id"])
    ],
)
class Inventory(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    var store: Store,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int,

    @Version
    var version: Long? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)