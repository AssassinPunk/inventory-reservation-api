package io.github.assassinpunk.inventory.controller

import io.github.assassinpunk.inventory.dto.AvailabilityResponse
import io.github.assassinpunk.inventory.dto.SetStockRequest
import io.github.assassinpunk.inventory.dto.StockResponse
import io.github.assassinpunk.inventory.service.InventoryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class InventoryController(private val inventoryService: InventoryService) {

    @PutMapping("/inventory")
    fun setStock(@Valid @RequestBody request: SetStockRequest): StockResponse =
        inventoryService.setStock(request)

    @GetMapping("/availability/{sku}")
    fun availability(
        @PathVariable sku: String,
        @RequestParam(required = false) pincode: String?,
    ): List<AvailabilityResponse> = inventoryService.availability(sku, pincode)
}