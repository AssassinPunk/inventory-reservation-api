package io.github.assassinpunk.inventory.dto

import io.github.assassinpunk.inventory.domain.Inventory
import io.github.assassinpunk.inventory.domain.Product
import io.github.assassinpunk.inventory.domain.Store
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import jakarta.validation.constraints.Max

// ---------- Requests (what clients send) ----------

data class CreateStoreRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:Pattern(regexp = "\\d{6}", message = "must be a 6-digit pincode")
    val pincode: String,
)

data class CreateProductRequest(
    @field:NotBlank
    @field:Size(max = 64)
    val sku: String,

    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:NotNull
    @field:DecimalMin(value = "0.00", inclusive = false)
    @field:Digits(integer = 8, fraction = 2)
    val price: BigDecimal,
)

data class SetStockRequest(
    @field:NotBlank
    val sku: String,

    @field:NotNull
    val storeId: Long,

    @field:Min(0)
    val quantity: Int,
)
data class ReserveRequest(
    @field:NotBlank
    val sku: String,

    @field:NotNull
    val storeId: Long,

    @field:Min(1)
    @field:Max(100)
    val quantity: Int,
)

data class ReservationResponse(
    val reservationId: Long,
    val sku: String,
    val storeId: Long,
    val quantity: Int,
    val remainingStock: Int,
)

// ---------- Responses (what the API returns) ----------

data class StoreResponse(val id: Long, val name: String, val pincode: String)

data class ProductResponse(val id: Long, val sku: String, val name: String, val price: BigDecimal)

data class StockResponse(val sku: String, val storeId: Long, val quantity: Int)

data class AvailabilityResponse(
    val storeId: Long,
    val storeName: String,
    val pincode: String,
    val quantity: Int,
)

// ---------- Entity -> response mapping (extension functions) ----------

fun Store.toResponse() = StoreResponse(id = id!!, name = name, pincode = pincode)

fun Product.toResponse() = ProductResponse(id = id!!, sku = sku, name = name, price = price)

fun Inventory.toStockResponse() = StockResponse(sku = product.sku, storeId = store.id!!, quantity = quantity)

fun Inventory.toAvailabilityResponse() = AvailabilityResponse(
    storeId = store.id!!,
    storeName = store.name,
    pincode = store.pincode,
    quantity = quantity,
)