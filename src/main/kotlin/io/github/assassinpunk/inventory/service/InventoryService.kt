package io.github.assassinpunk.inventory.service

import io.github.assassinpunk.inventory.domain.Inventory
import io.github.assassinpunk.inventory.dto.AvailabilityResponse
import io.github.assassinpunk.inventory.dto.SetStockRequest
import io.github.assassinpunk.inventory.dto.StockResponse
import io.github.assassinpunk.inventory.dto.toAvailabilityResponse
import io.github.assassinpunk.inventory.dto.toStockResponse
import io.github.assassinpunk.inventory.exception.NotFoundException
import io.github.assassinpunk.inventory.repository.InventoryRepository
import io.github.assassinpunk.inventory.repository.ProductRepository
import io.github.assassinpunk.inventory.repository.StoreRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
) {

    /** Creates the stock row if it doesn't exist, otherwise overwrites the quantity (an "upsert"). */
    @Transactional
    fun setStock(request: SetStockRequest): StockResponse {
        val sku = request.sku.trim().uppercase()
        val product = productRepository.findBySku(sku) ?: throw NotFoundException("Product '$sku' not found")
        val store = storeRepository.findByIdOrNull(request.storeId)
            ?: throw NotFoundException("Store ${request.storeId} not found")

        val inventory = inventoryRepository.findByProductSkuAndStoreId(sku, request.storeId)
            ?.apply { quantity = request.quantity }
            ?: Inventory(store = store, product = product, quantity = request.quantity)

        return inventoryRepository.save(inventory).toStockResponse()
    }

    @Transactional(readOnly = true)
    fun availability(sku: String, pincode: String?): List<AvailabilityResponse> {
        val normalizedSku = sku.trim().uppercase()
        if (!productRepository.existsBySku(normalizedSku)) {
            throw NotFoundException("Product '$normalizedSku' not found")
        }
        val rows = if (pincode.isNullOrBlank()) {
            inventoryRepository.findInStockBySku(normalizedSku)
        } else {
            inventoryRepository.findInStockBySkuAndPincode(normalizedSku, pincode.trim())
        }
        return rows.map { it.toAvailabilityResponse() }
    }
}