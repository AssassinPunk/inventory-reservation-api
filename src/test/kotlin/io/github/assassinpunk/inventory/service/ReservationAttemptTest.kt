package io.github.assassinpunk.inventory.service

import io.github.assassinpunk.inventory.domain.Inventory
import io.github.assassinpunk.inventory.domain.Product
import io.github.assassinpunk.inventory.domain.Reservation
import io.github.assassinpunk.inventory.domain.Store
import io.github.assassinpunk.inventory.exception.NotFoundException
import io.github.assassinpunk.inventory.repository.InventoryRepository
import io.github.assassinpunk.inventory.repository.ProductRepository
import io.github.assassinpunk.inventory.repository.ReservationRepository
import io.github.assassinpunk.inventory.repository.StoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class ReservationAttemptTest {

    private val inventoryRepository: InventoryRepository = mock()
    private val productRepository: ProductRepository = mock()
    private val storeRepository: StoreRepository = mock()
    private val reservationRepository: ReservationRepository = mock()

    private val attempt = ReservationAttempt(
        inventoryRepository, productRepository, storeRepository, reservationRepository,
    )

    private fun inventoryWith(quantity: Int) = Inventory(
        store = Store(name = "Koramangala", pincode = "560034", id = 1L),
        product = Product(sku = "TSHIRT-01", name = "Cotton T-Shirt", price = BigDecimal("499.00"), id = 1L),
        quantity = quantity,
        id = 1L,
    )

    @Test
    fun `reserves stock, decrements quantity and records the reservation`() {
        val inventory = inventoryWith(quantity = 5)
        whenever(inventoryRepository.findByProductSkuAndStoreId("TSHIRT-01", 1L)).thenReturn(inventory)
        whenever(reservationRepository.save(any<Reservation>()))
            .thenAnswer { it.getArgument<Reservation>(0).apply { id = 42L } }

        val result = attempt.tryReserve("TSHIRT-01", 1L, 3)

        assertThat(result).isEqualTo(
            ReservationResult.Success(reservationId = 42L, sku = "TSHIRT-01", storeId = 1L, quantity = 3, remainingStock = 2)
        )
        assertThat(inventory.quantity).isEqualTo(2)
    }

    @Test
    fun `returns InsufficientStock and saves nothing when stock is too low`() {
        whenever(inventoryRepository.findByProductSkuAndStoreId("TSHIRT-01", 1L)).thenReturn(inventoryWith(quantity = 2))

        val result = attempt.tryReserve("TSHIRT-01", 1L, 3)

        assertThat(result).isEqualTo(ReservationResult.InsufficientStock(requested = 3, available = 2))
        verify(reservationRepository, never()).save(any<Reservation>())
    }

    @Test
    fun `treats a missing stock row as zero available when product and store exist`() {
        whenever(inventoryRepository.findByProductSkuAndStoreId("TSHIRT-01", 1L)).thenReturn(null)
        whenever(productRepository.existsBySku("TSHIRT-01")).thenReturn(true)
        whenever(storeRepository.existsById(1L)).thenReturn(true)

        val result = attempt.tryReserve("TSHIRT-01", 1L, 1)

        assertThat(result).isEqualTo(ReservationResult.InsufficientStock(requested = 1, available = 0))
    }

    @Test
    fun `throws NotFoundException for an unknown product`() {
        whenever(inventoryRepository.findByProductSkuAndStoreId("NOPE-99", 1L)).thenReturn(null)
        whenever(productRepository.existsBySku("NOPE-99")).thenReturn(false)

        assertThrows<NotFoundException> { attempt.tryReserve("NOPE-99", 1L, 1) }
    }
}