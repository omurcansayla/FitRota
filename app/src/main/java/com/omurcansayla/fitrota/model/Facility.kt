package com.omurcansayla.fitrota.model

data class Facility(
    val facilityId: String = "",
    val name: String = "",
    val description: String = "",
    val pricePerHour: Double = 0.0,
    val imageUrl: String = "",
    // En önemli kısım burası: Varsayılan olarak 0.0 veriyoruz
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val availableHours: List<String> = emptyList(),
    val category: String="Genel"
)