package com.yarsi.rescuepet.utils

fun maskContact(contact: String): String {
    if (contact.length <= 4) return contact
    val first = contact.take(4)
    val last = if (contact.length >= 8) contact.takeLast(4) else ""
    val mid = contact.length - 4 - last.length
    return first + "*".repeat(mid.coerceAtLeast(0)) + last
}
