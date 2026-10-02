package io.github.assassinpunk.inventory.service

import io.github.assassinpunk.inventory.dto.ReserveRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.OptimisticLockingFailureException

class ReservationServiceTest {

    private val attempt: ReservationAttempt = mock()
    private val service = ReservationService(reservationAttempt = attempt, maxAttempts = 3)

    private val success = ReservationResult.Success(
        reservationId = 1L, sku = "TSHIRT-01", storeId = 1L, quantity = 1, remainingStock = 9,
    )

    @Test
    fun `returns the result of the first attempt when there is no conflict`() {
        whenever(attempt.tryReserve("TSHIRT-01", 1L, 1)).thenReturn(success)

        val result = service.reserve(ReserveRequest(sku = "TSHIRT-01", storeId = 1L, quantity = 1))

        assertThat(result).isEqualTo(success)
        verify(attempt, times(1)).tryReserve("TSHIRT-01", 1L, 1)
    }

    @Test
    fun `normalizes the SKU before reserving`() {
        whenever(attempt.tryReserve("TSHIRT-01", 1L, 1)).thenReturn(success)

        service.reserve(ReserveRequest(sku = "  tshirt-01 ", storeId = 1L, quantity = 1))

        verify(attempt).tryReserve("TSHIRT-01", 1L, 1)
    }

    @Test
    fun `retries after an optimistic lock conflict and then succeeds`() {
        whenever(attempt.tryReserve("TSHIRT-01", 1L, 1))
            .thenThrow(OptimisticLockingFailureException("version conflict"))
            .thenReturn(success)

        val result = service.reserve(ReserveRequest(sku = "TSHIRT-01", storeId = 1L, quantity = 1))

        assertThat(result).isEqualTo(success)
        verify(attempt, times(2)).tryReserve("TSHIRT-01", 1L, 1)
    }

    @Test
    fun `gives up with Conflict after max attempts`() {
        whenever(attempt.tryReserve("TSHIRT-01", 1L, 1))
            .thenThrow(OptimisticLockingFailureException("version conflict"))

        val result = service.reserve(ReserveRequest(sku = "TSHIRT-01", storeId = 1L, quantity = 1))

        assertThat(result).isEqualTo(ReservationResult.Conflict)
        verify(attempt, times(3)).tryReserve("TSHIRT-01", 1L, 1)
    }
}