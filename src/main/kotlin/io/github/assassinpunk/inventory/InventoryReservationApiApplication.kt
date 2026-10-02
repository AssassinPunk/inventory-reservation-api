package io.github.assassinpunk.inventory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootApplication
class InventoryReservationApiApplication

fun main(args: Array<String>) {
	// Run the backend in UTC so timestamps are consistent across machines and the DB
	TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
	runApplication<InventoryReservationApiApplication>(*args)
}