package com.yi.labbooking

import kotlinx.serialization.Serializable

@Serializable
data class Equipment(val equipmentId: String, val name: String, val category: String, val location: String) {
    init {
        require(equipmentId.isNotBlank()) { "Equipment ID is required." }
        require(name.isNotBlank()) { "Equipment name is required." }
        require(category.isNotBlank()) { "Category is required." }
        require(location.isNotBlank()) { "Location is required." }
    }
}
