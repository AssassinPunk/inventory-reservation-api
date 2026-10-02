package io.github.assassinpunk.inventory.controller

import io.github.assassinpunk.inventory.dto.CreateStoreRequest
import io.github.assassinpunk.inventory.dto.StoreResponse
import io.github.assassinpunk.inventory.service.CatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stores")
class StoreController(private val catalogService: CatalogService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateStoreRequest): StoreResponse =
        catalogService.createStore(request)

    @GetMapping
    fun list(): List<StoreResponse> = catalogService.listStores()
}