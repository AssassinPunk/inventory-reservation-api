package io.github.assassinpunk.inventory

import io.github.assassinpunk.inventory.domain.Inventory
import io.github.assassinpunk.inventory.domain.Product
import io.github.assassinpunk.inventory.domain.Store
import io.github.assassinpunk.inventory.dto.ReserveRequest
import io.github.assassinpunk.inventory.repository.InventoryRepository
import io.github.assassinpunk.inventory.repository.ProductRepository
import io.github.assassinpunk.inventory.repository.ReservationRepository
import io.github.assassinpunk.inventory.repository.StoreRepository
import io.github.assassinpunk.inventory.service.ReservationResult
import io.github.assassinpunk.inventory.service.ReservationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.math.BigDecimal
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// More attempts than production so that, under 20-way contention, every request
// settles as either Success or InsufficientStock and the counts are deterministic.
@SpringBootTest(properties = ["inventory.reservation.max-attempts=20"])
@Import(TestcontainersConfiguration::class)
class ReservationIntegrationTest @Autowired constructor(
    private val reservationService: ReservationService,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val reservationRepository: ReservationRepository,
) {

    @BeforeEach
    fun cleanDatabase() {
        reservationRepository.deleteAll()
        inventoryRepository.deleteAll()
        productRepository.deleteAll()
        storeRepository.deleteAll()
    }

    @Test
    fun `20 parallel reservations for 10 units never oversell`() {
        val store = storeRepository.save(Store(name = "Koramangala", pincode = "560034"))
        val product = productRepository.save(Product(sku = "TSHIRT-01", name = "Cotton T-Shirt", price = BigDecimal("499.00")))
        inventoryRepository.save(Inventory(store = store, product = product, quantity = 10))

        val threads = 20
        val pool = Executors.newFixedThreadPool(threads)
        val ready = CountDownLatch(threads)   // every thread is waiting at the gate
        val start = CountDownLatch(1)         // open the gate for all at once

        val futures = (1..threads).map {
            pool.submit(Callable {
                ready.countDown()
                start.await()
                reservationService.reserve(ReserveRequest(sku = "TSHIRT-01", storeId = store.id!!, quantity = 1))
            })
        }

        ready.await()
        start.countDown()
        val results = futures.map { it.get(60, TimeUnit.SECONDS) }
        pool.shutdown()

        val inventory = inventoryRepository.findByProductSkuAndStoreId("TSHIRT-01", store.id!!)!!

        assertThat(results.count { it is ReservationResult.Success }).isEqualTo(10)
        assertThat(results.count { it is ReservationResult.InsufficientStock }).isEqualTo(10)
        assertThat(results.count { it == ReservationResult.Conflict }).isEqualTo(0)
        assertThat(inventory.quantity).isEqualTo(0)
        assertThat(inventory.version).isEqualTo(10L)
        assertThat(reservationRepository.count()).isEqualTo(10L)
    }

    @Test
    fun `availability returns only in-stock stores and filters by pincode`() {
        val product = productRepository.save(Product(sku = "MUG-01", name = "Coffee Mug", price = BigDecimal("199.00")))
        val koramangala = storeRepository.save(Store(name = "Koramangala", pincode = "560034"))
        val indiranagar = storeRepository.save(Store(name = "Indiranagar", pincode = "560038"))
        inventoryRepository.save(Inventory(store = koramangala, product = product, quantity = 5))
        inventoryRepository.save(Inventory(store = indiranagar, product = product, quantity = 0))

        val inStock = inventoryRepository.findInStockBySku("MUG-01")
        val nearIndiranagar = inventoryRepository.findInStockBySkuAndPincode("MUG-01", "560038")

        assertThat(inStock.map { it.store.name }).containsExactly("Koramangala")
        assertThat(nearIndiranagar).isEmpty()
    }
}