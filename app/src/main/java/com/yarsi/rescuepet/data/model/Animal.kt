package com.yarsi.rescuepet.data.model

data class Animal(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val age: Int = 0,
    val description: String = "",
    val imageId: String = "",
    val status: String = "available",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val posterId: String = "",
    val posterName: String = "",
    val posterContact: String = "",
    val category: String = "adoption"
)
