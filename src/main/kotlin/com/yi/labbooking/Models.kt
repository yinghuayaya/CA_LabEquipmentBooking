package com.yi.labbooking

import java.time.LocalDateTime

enum class Status { ACTIVE, CANCELLED }

data class Reservation(
    val equipmentId: String,
    val requester: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val purpose: String,
) {
    init {
        require(equipmentId.isNotBlank()) { "Equipment ID is required." }
        require(requester.isNotBlank()) { "Requester is required." }
        require(purpose.isNotBlank()) { "Purpose is required." }
        require(startTime < endTime) { "Start must be earlier than end." }
    }
}

data class Booking(val bookingId: String, val slot: Reservation, val status: Status)
