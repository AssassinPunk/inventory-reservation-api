package io.github.assassinpunk.inventory.repository

import io.github.assassinpunk.inventory.domain.Inventory
import io.github.assassinpunk.inventory.domain.Product
import io.github.assassinpunk.inventory.domain.Store
import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository : JpaRepository<Store, Long>

interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySku(sku: String): Product?
}

interface InventoryRepository : JpaRepository<Inventory, Long> {
    fun findByProductSkuAndStoreId(sku: String, storeId: Long): Inventory?
}