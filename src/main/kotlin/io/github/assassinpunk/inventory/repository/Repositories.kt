package io.github.assassinpunk.inventory.repository

import io.github.assassinpunk.inventory.domain.Inventory
import io.github.assassinpunk.inventory.domain.Product
import io.github.assassinpunk.inventory.domain.Store
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StoreRepository : JpaRepository<Store, Long>

interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySku(sku: String): Product?
    fun existsBySku(sku: String): Boolean
}

interface InventoryRepository : JpaRepository<Inventory, Long> {
    fun findByProductSkuAndStoreId(sku: String, storeId: Long): Inventory?

    // "join fetch" loads store + product in the SAME query, avoiding the N+1 problem
    @Query(
        """
        select i from Inventory i
        join fetch i.store s
        join fetch i.product p
        where p.sku = :sku and i.quantity > 0
        order by s.id
        """
    )
    fun findInStockBySku(sku: String): List<Inventory>

    @Query(
        """
        select i from Inventory i
        join fetch i.store s
        join fetch i.product p
        where p.sku = :sku and s.pincode = :pincode and i.quantity > 0
        order by s.id
        """
    )
    fun findInStockBySkuAndPincode(sku: String, pincode: String): List<Inventory>
}