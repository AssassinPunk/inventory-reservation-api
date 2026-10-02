package io.github.assassinpunk.inventory.service

import io.github.assassinpunk.inventory.domain.Product
import io.github.assassinpunk.inventory.domain.Store
import io.github.assassinpunk.inventory.dto.CreateProductRequest
import io.github.assassinpunk.inventory.dto.CreateStoreRequest
import io.github.assassinpunk.inventory.dto.ProductResponse
import io.github.assassinpunk.inventory.dto.StoreResponse
import io.github.assassinpunk.inventory.dto.toResponse
import io.github.assassinpunk.inventory.exception.ConflictException
import io.github.assassinpunk.inventory.exception.NotFoundException
import io.github.assassinpunk.inventory.repository.ProductRepository
import io.github.assassinpunk.inventory.repository.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CatalogService(
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
) {

    @Transactional
    fun createStore(request: CreateStoreRequest): StoreResponse =
        storeRepository.save(Store(name = request.name.trim(), pincode = request.pincode)).toResponse()

    @Transactional(readOnly = true)
    fun listStores(): List<StoreResponse> = storeRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductResponse {
        val sku = request.sku.trim().uppercase()
        if (productRepository.existsBySku(sku)) {
            throw ConflictException("Product with SKU '$sku' already exists")
        }
        return productRepository.save(Product(sku = sku, name = request.name.trim(), price = request.price)).toResponse()
    }

    @Transactional(readOnly = true)
    fun getProduct(sku: String): ProductResponse =
        (productRepository.findBySku(sku.trim().uppercase()) ?: throw NotFoundException("Product '$sku' not found"))
            .toResponse()
}