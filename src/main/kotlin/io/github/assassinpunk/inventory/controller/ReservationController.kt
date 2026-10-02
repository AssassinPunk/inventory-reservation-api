package io.github.assassinpunk.inventory.controller

import io.github.assassinpunk.inventory.dto.ReservationResponse
import io.github.assassinpunk.inventory.dto.ReserveRequest
import io.github.assassinpunk.inventory.service.ReservationResult
import io.github.assassinpunk.inventory.service.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reservations")
class ReservationController(private val reservationService: ReservationService) {

    @PostMapping
    fun reserve(@Valid @RequestBody request: ReserveRequest): ResponseEntity<Any> =
        when (val result = reservationService.reserve(request)) {
            is ReservationResult.Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())

            is ReservationResult.InsufficientStock -> {
                val problem = ProblemDetail.forStatusAndDetail(
                    HttpStatusCode.valueOf(422),
                    "Only ${result.available} unit(s) available, ${result.requested} requested",
                )
                problem.setProperty("available", result.available)
                ResponseEntity.status(HttpStatusCode.valueOf(422)).body(problem)
            }

            ReservationResult.Conflict ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Stock changed concurrently, please retry")
                )
        }

    private fun ReservationResult.Success.toResponse() = ReservationResponse(
        reservationId = reservationId,
        sku = sku,
        storeId = storeId,
        quantity = quantity,
        remainingStock = remainingStock,
    )
}