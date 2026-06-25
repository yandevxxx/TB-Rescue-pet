package com.yarsi.rescuepet.utils

fun validateContact(contact: String): String? {
    if (contact.length < 6) return "Kontak minimal 6 karakter"
    val isPhone = Regex("^[+]?[0-9]{8,15}$").matches(contact)
    val isEmail = Regex("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$").matches(contact)
    if (!isPhone && !isEmail) return "Masukkan nomor HP atau email yang valid"
    return null
}

fun validateLatitude(lat: String): String? {
    if (lat.isBlank()) return null
    val value = lat.toDoubleOrNull() ?: return "Latitude tidak valid"
    if (value < -90.0 || value > 90.0) return "Latitude harus antara -90 dan 90"
    return null
}

fun validateLongitude(lon: String): String? {
    if (lon.isBlank()) return null
    val value = lon.toDoubleOrNull() ?: return "Longitude tidak valid"
    if (value < -180.0 || value > 180.0) return "Longitude harus antara -180 dan 180"
    return null
}
