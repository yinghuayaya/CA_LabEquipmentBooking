package com.yi.labbooking

class BookingService {
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
        equipment.remove(id)
    }
}
