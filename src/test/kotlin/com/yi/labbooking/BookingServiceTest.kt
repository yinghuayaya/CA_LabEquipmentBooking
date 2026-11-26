package com.yi.labbooking

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class BookingServiceTest {
    private val service = BookingService().apply {
        addEquipment(Equipment("A", "Scope", "Optics", "Bench"))
        addEquipment(Equipment("B", "Lens", "Optics", "Bench"))
    }
    private val day = LocalDateTime.parse("2026-12-01T00:00")
    private fun slot(start: Long, end: Long, id: String = "A") =
        Reservation(id, "Yao", day.plusHours(start), day.plusHours(end), "Project")

    @ParameterizedTest
    @CsvSource("9,12", "8,10", "10,13", "8,13", "10,11")
    fun `reject conflicts with either neighbor and preserve sequence`(s: Long, e: Long) {
        service.createBooking(slot(9, 12))
        assertThrows(IllegalArgumentException::class.java) { service.createBooking(slot(s, e)) }
        assertEquals("BK-0002", service.createBooking(slot(12, 13)).bookingId)
    }

    @Test
    fun `adjacent slots and different equipment can be booked`() {
        val original = service.createBooking(slot(10, 12))
        service.createBooking(slot(8, 10))
        service.createBooking(slot(12, 13))
        service.createBooking(slot(10, 12, "B"))
        assertEquals(3, service.listBookings("A", Status.ACTIVE).size)
        val cancelled = service.cancelBooking(original.bookingId)
        assertEquals(listOf(cancelled), service.listBookings(status = Status.CANCELLED))
        assertEquals(original.slot, service.createBooking(original.slot).slot)
        assertThrows(IllegalArgumentException::class.java) { service.cancelBooking(original.bookingId) }
    }

    @Test
    fun `invalid requests and missing equipment are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { service.createBooking(slot(9, 10, "missing")) }
        assertThrows(IllegalArgumentException::class.java) { slot(10, 10) }
        assertThrows(IllegalArgumentException::class.java) { slot(12, 10) }
        assertThrows(IllegalArgumentException::class.java) { slot(9, 10).copy(requester = " ") }
        assertThrows(IllegalArgumentException::class.java) { slot(9, 10).copy(purpose = " ") }
        assertThrows(IllegalArgumentException::class.java) { service.cancelBooking("missing") }
        assertEquals(emptyList<Booking>(), service.listBookings())
    }

    @Test
    fun `deletion preserves both active and cancelled history`() {
        val booking = service.createBooking(slot(9, 12))
        assertThrows(IllegalArgumentException::class.java) { service.deleteEquipment("A") }
        service.cancelBooking(booking.bookingId)
        assertThrows(IllegalArgumentException::class.java) { service.deleteEquipment("A") }
    }
}
