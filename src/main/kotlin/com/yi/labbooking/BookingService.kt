package com.yi.labbooking

import java.time.LocalDateTime
import java.util.TreeMap

class BookingService {
    private val bookings = HashMap<String, Booking>()
    private val schedules = HashMap<String, TreeMap<LocalDateTime, String>>()
    var nextSequence = 1L
        private set

    fun createBooking(slot: Reservation): Booking {
        require(nextSequence < Long.MAX_VALUE) { "Booking ID sequence exhausted." }
        val booking = Booking(bookingId(nextSequence), slot, Status.ACTIVE)
        insert(booking)
        nextSequence++
        return booking
    }

    private fun insert(booking: Booking) {
        val slot = booking.slot
        showEquipment(slot.equipmentId)
        require(booking.bookingId !in bookings) { "Duplicate booking ID: ${booking.bookingId}" }
        if (booking.status == Status.ACTIVE) {
            val schedule = schedules.getOrPut(slot.equipmentId) { TreeMap() }
            val previous = schedule.floorEntry(slot.startTime)?.value?.let { bookings.getValue(it) }
            val next = schedule.ceilingEntry(slot.startTime)?.key
            require(previous == null || previous.slot.endTime <= slot.startTime) { "Overlapping active booking." }
            require(next == null || slot.endTime <= next) { "Overlapping active booking." }
            schedule[slot.startTime] = booking.bookingId
        }
        bookings[booking.bookingId] = booking
    }

    fun listBookings(equipmentId: String? = null, status: Status? = null) = bookings.values.filter {
        (equipmentId == null || it.slot.equipmentId == equipmentId) && (status == null || it.status == status)
    }.sortedBy { it.bookingId.removePrefix("BK-").toLong() }

    fun cancelBooking(id: String): Booking {
        val booking = requireNotNull(bookings[id]) { "Unknown booking: $id" }
        require(booking.status == Status.ACTIVE) { "Booking $id is already cancelled." }
        schedules.getValue(booking.slot.equipmentId).remove(booking.slot.startTime)
        return booking.copy(status = Status.CANCELLED).also { bookings[id] = it }
    }

    private fun bookingId(number: Long) = "BK-" + number.toString().padStart(ID_WIDTH, '0')

    companion object {
        private const val ID_WIDTH = 4

        internal fun restore(items: List<Equipment>, history: List<Booking>, next: Long): BookingService {
            require(next > 0) { "Invalid booking sequence." }
            val service = BookingService()
            items.forEach(service::addEquipment)
            for (booking in history) {
                val number = booking.bookingId.removePrefix("BK-").toLongOrNull()
                require(number != null && number in 1 until next && service.bookingId(number) == booking.bookingId) {
                    "Invalid booking ID or sequence: ${booking.bookingId}"
                }
                service.insert(booking)
            }
            service.nextSequence = next
            return service
        }
    }

    private val equipment = HashMap<String, Equipment>()

    fun addEquipment(item: Equipment) {
        require(equipment.putIfAbsent(item.equipmentId, item) == null) { "Duplicate equipment ID: ${item.equipmentId}" }
    }

    fun showEquipment(id: String): Equipment = requireNotNull(equipment[id]) { "Unknown equipment: $id" }

    fun listEquipment() = equipment.values.sortedBy { it.equipmentId }

    fun searchEquipment(query: String): List<Equipment> {
        require(query.isNotBlank()) { "Search query is required." }
        return listEquipment().filter { item ->
            listOf(item.equipmentId, item.name, item.category, item.location)
                .any { it.contains(query, ignoreCase = true) }
        }
    }

    fun deleteEquipment(id: String) {
        showEquipment(id)
        require(bookings.values.none { it.slot.equipmentId == id }) { "Equipment $id has booking history." }
        equipment.remove(id)
    }
}
