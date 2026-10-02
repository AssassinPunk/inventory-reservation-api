package io.github.assassinpunk.inventory.service

import io.github.assassinpunk.inventory.domain.Reservation
import io.github.assassinpunk.inventory.exception.NotFoundException
import io.github.assassinpunk.inventory.repository.InventoryRepository
import io.github.assassinpunk.inventory.repository.ProductRepository
import io.github.assassinpunk.inventory.repository.ReservationRepository
import io.github.assassinpunk.inventory.repository.StoreRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * One reservation attempt in its own transaction.
 * Kept in a separate bean so every call goes through Spring's transactional proxy.
 */
@Component
class ReservationAttempt(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val reservationRepository: ReservationRepository,
) {

    @Transactional
    fun tryReserve(sku: String, storeId: Long, quantity: Int): ReservationResult {
        val inventory = inventoryRepository.findByProductSkuAndStoreId(sku, storeId)

        if (inventory == null) {
            if (!productRepository.existsBySku(sku)) throw NotFoundException("Product '$sku' not found")
            if (!storeRepository.existsById(storeId)) throw NotFoundException("Store $storeId not found")
            return ReservationResult.InsufficientStock(requested = quantity, available = 0)
        }

        if (inventory.quantity < quantity) {
            return ReservationResult.InsufficientStock(requested = quantity, available = inventory.quantity)
        }

        // Hibernate will run: UPDATE inventory SET quantity = ?, version = version + 1 WHERE id = ? AND version = ?
        inventory.quantity -= quantity

        val reservation = reservationRepository.save(
            Reservation(store = inventory.store, product = inventory.product, quantity = quantity)
        )

        return ReservationResult.Success(
            reservationId = reservation.id!!,
            sku = sku,
            storeId = storeId,
            quantity = quantity,
            remainingStock = inventory.quantity,
        )
    }
}