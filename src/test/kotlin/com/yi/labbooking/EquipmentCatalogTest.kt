package com.yi.labbooking

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EquipmentCatalogTest {
    private val service = BookingService()
    private val projector = Equipment("EQ-2", "Projector", "AV", "Lab A")
    private val scope = Equipment("EQ-1", "Oscilloscope", "Electronics", "Lab B")

    @Test
    fun `duplicate ID preserves the original equipment`() {
        service.addEquipment(projector)
        assertThrows(IllegalArgumentException::class.java) { service.addEquipment(projector.copy(name = "Other")) }
        assertEquals(projector, service.showEquipment("EQ-2"))
    }

    @Test
    fun `catalog is sorted and deletion removes only the selected item`() {
        service.addEquipment(projector)
        service.addEquipment(scope)
        assertEquals(listOf(scope, projector), service.listEquipment())
        service.deleteEquipment("EQ-2")
        assertEquals(listOf(scope), service.listEquipment())
        assertThrows(IllegalArgumentException::class.java) { service.deleteEquipment("EQ-2") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["eq-2", "PROJECT", "av", "lab a"])
    fun `search matches every catalog field without case sensitivity`(query: String) {
        service.addEquipment(projector)
        service.addEquipment(scope)
        assertEquals(listOf(projector), service.searchEquipment(query))
    }

    @Test
    fun `blank fields and blank search are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { projector.copy(equipmentId = " ") }
        assertThrows(IllegalArgumentException::class.java) { projector.copy(name = "") }
        assertThrows(IllegalArgumentException::class.java) { projector.copy(category = " ") }
        assertThrows(IllegalArgumentException::class.java) { projector.copy(location = "") }
        assertThrows(IllegalArgumentException::class.java) { service.searchEquipment(" ") }
    }
}
