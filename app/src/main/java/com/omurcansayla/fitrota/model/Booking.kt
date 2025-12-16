package com.omurcansayla.fitrota.model

data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val date: String = "",
    val time: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Onaylandı"
)