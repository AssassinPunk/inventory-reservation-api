package io.github.assassinpunk.inventory.service

/** Every possible outcome of a reservation. "sealed" = no other outcomes can exist. */
sealed interface ReservationResult {

    data class Success(
        val reservationId: Long,
        val sku: String,
        val storeId: Long,
        val quantity: Int,
        val remainingStock: Int,
    ) : ReservationResult

    data class InsufficientStock(val requested: Int, val available: Int) : ReservationResult

    /** Every retry lost the race to other requests. */
    data object Conflict : ReservationResult
}