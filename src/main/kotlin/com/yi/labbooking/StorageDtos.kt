package com.yi.labbooking

import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
internal data class DatabaseDto(
    val nextSequence: Long,
    val equipment: List<Equipment>,
    val bookings: List<BookingDto>,
) {
    fun restore() = BookingService.restore(equipment, bookings.map { it.toBooking() }, nextSequence)

    companion object {
        fun from(service: BookingService) = DatabaseDto(
            service.nextSequence, service.listEquipment(), service.listBookings().map(BookingDto::from),
        )
    }
}

@Serializable
internal data class BookingDto(
    val bookingId: String,
    val equipmentId: String,
    val requester: String,
    val startTime: String,
    val endTime: String,
    val purpose: String,
    val status: Status,
) {
    fun toBooking() = Booking(
        bookingId,
        Reservation(equipmentId, requester, LocalDateTime.parse(startTime), LocalDateTime.parse(endTime), purpose),
        status,
    )

    companion object {
        fun from(booking: Booking) = with(booking.slot) {
            BookingDto(
                booking.bookingId, equipmentId, requester, startTime.toString(), endTime.toString(), purpose,
                booking.status,
            )
        }
    }
}
