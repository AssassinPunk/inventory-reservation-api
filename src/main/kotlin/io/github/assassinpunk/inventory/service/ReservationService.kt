package io.github.assassinpunk.inventory.service

import io.github.assassinpunk.inventory.dto.ReserveRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.util.concurrent.ThreadLocalRandom

@Service
class ReservationService(
    private val reservationAttempt: ReservationAttempt,
    @Value("\${inventory.reservation.max-attempts:5}") private val maxAttempts: Int,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Deliberately NOT @Transactional: each retry needs a brand-new transaction with fresh data. */
    fun reserve(request: ReserveRequest): ReservationResult {
        val sku = request.sku.trim().uppercase()

        repeat(maxAttempts) { attempt ->
            try {
                return reservationAttempt.tryReserve(sku, request.storeId, request.quantity)
            } catch (ex: OptimisticLockingFailureException) {
                log.info("Optimistic lock conflict on {} at store {} (attempt {}/{})", sku, request.storeId, attempt + 1, maxAttempts)
                backoff(attempt)
            }
        }

        log.warn("Giving up on {} at store {} after {} attempts", sku, request.storeId, maxAttempts)
        return ReservationResult.Conflict
    }

    /** Wait a short, random, growing time so competing requests don't collide again in lockstep. */
    private fun backoff(attempt: Int) {
        Thread.sleep(ThreadLocalRandom.current().nextLong(5, 25) * (attempt + 1))
    }
}