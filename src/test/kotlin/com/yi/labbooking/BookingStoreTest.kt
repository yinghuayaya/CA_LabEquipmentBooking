package com.yi.labbooking

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BookingStoreTest(@TempDir val directory: Path) {
    private val file = directory.resolve("nested/state.json")
    private val item = Equipment("A", "Scope", "Optics", "Lab")
    private val start = LocalDateTime.parse("2026-12-01T09:00")
    private val slot = Reservation("A", "Yao", start, start.plusHours(1), "Project")

    @Test
    fun `initialize missing data and restore history sequence and active slots`() {
        assertEquals(emptyList<Equipment>(), BookingStore(file).read { it.listEquipment() })
        assertTrue(Files.isRegularFile(file))
        BookingStore(file).update {
            it.addEquipment(item)
            it.cancelBooking(it.createBooking(slot).bookingId)
            it.createBooking(slot)
        }
        val reopened = BookingStore(file)
        reopened.read {
            assertEquals(item, it.showEquipment("A"))
            assertEquals(listOf(Status.CANCELLED, Status.ACTIVE), it.listBookings().map(Booking::status))
        }
        assertThrows(IllegalArgumentException::class.java) { reopened.update { it.createBooking(slot) } }
        val later = slot.copy(startTime = slot.endTime, endTime = slot.endTime.plusHours(1))
        assertEquals("BK-0003", reopened.update { it.createBooking(later) }.bookingId)
    }

    @ParameterizedTest
    @ValueSource(strings = ["broken JSON", "{}", """{"nextSequence":0,"equipment":[],"bookings":[]}"""])
    fun `invalid data cannot be overwritten by a mutation`(invalid: String) {
        Files.createDirectories(file.parent)
        Files.writeString(file, invalid)
        assertThrows(IOException::class.java) { BookingStore(file).update { it.addEquipment(item) } }
        assertEquals(invalid, Files.readString(file))
    }

    @Test
    fun `reject persisted conflicts and stale sequences`() {
        BookingStore(file).update {
            it.addEquipment(item)
            it.createBooking(slot)
            it.createBooking(slot.copy(startTime = start.plusDays(1), endTime = slot.endTime.plusDays(1)))
        }
        val original = Files.readString(file)
        val overlap = original.replace("2026-12-02", "2026-12-01")
        for (invalid in listOf(overlap, original.replace("BK-0002", "BK-0003"))) {
            Files.writeString(file, invalid)
            assertThrows(IOException::class.java) { BookingStore(file).read { it.listBookings() } }
            assertEquals(invalid, Files.readString(file))
        }
    }

    @Test
    fun `aborted transaction keeps disk state and invalid parent fails clearly`() {
        BookingStore(file).read { it.listEquipment() }
        assertThrows(IllegalArgumentException::class.java) {
            BookingStore(file).update {
                it.addEquipment(item)
                it.addEquipment(item)
            }
        }
        assertEquals(emptyList<Equipment>(), BookingStore(file).read { it.listEquipment() })
        assertThrows(IOException::class.java) { BookingStore(file.resolve("child")).read { it.listEquipment() } }
    }
}
