package com.yarsi.rescuepet.utils

import io.appwrite.exceptions.AppwriteException

object ErrorMapper {

    fun map(e: Exception, fallback: String = "Terjadi kesalahan"): String {
        val message = e.message ?: return fallback
        return when {
            message.contains("email already exists", ignoreCase = true) ->
                "Email sudah terdaftar"
            message.contains("user with this email", ignoreCase = true) ||
            message.contains("user already exists", ignoreCase = true) ->
                "Email sudah terdaftar"
            message.contains("Invalid credentials", ignoreCase = true) ||
            message.contains("wrong password", ignoreCase = true) ||
            message.contains("password is invalid", ignoreCase = true) ||
            message.contains("invalid email", ignoreCase = true) ->
                "Email atau password salah"
            message.contains("user not found", ignoreCase = true) ||
            message.contains("no user", ignoreCase = true) ->
                "Akun tidak ditemukan"
            message.contains("document with the requested ID", ignoreCase = true) &&
            message.contains("not found", ignoreCase = true) ->
                "Data tidak ditemukan"
            message.contains("permission", ignoreCase = true) ||
            message.contains("unauthorized", ignoreCase = true) ->
                "Anda tidak memiliki izin untuk melakukan aksi ini"
            message.contains("rate", ignoreCase = true) &&
            message.contains("limit", ignoreCase = true) ||
            message.contains("too many requests", ignoreCase = true) ->
                "Terlalu banyak permintaan, coba beberapa saat lagi"
            message.contains("network", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("unreachable", ignoreCase = true) ||
            message.contains("Failed to connect", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) ->
                "Tidak dapat terhubung ke server, periksa koneksi internet"
            message.contains("storage", ignoreCase = true) &&
            message.contains("not found", ignoreCase = true) ->
                "File tidak ditemukan"
            message.contains("payload too large", ignoreCase = true) ||
            message.contains("file size", ignoreCase = true) ->
                "Ukuran file terlalu besar"
            message.contains("Validation error", ignoreCase = true) ||
            message.contains("Invalid document", ignoreCase = true) ||
            message.contains("invalid structure", ignoreCase = true) ->
                "Data yang dikirim tidak valid, periksa kembali input"
            message.contains("session", ignoreCase = true) &&
            message.contains("not found", ignoreCase = true) ->
                "Sesi berakhir, silakan login ulang"
            message.contains("general", ignoreCase = true) &&
            message.contains("protection", ignoreCase = true) ->
                "Terjadi kesalahan keamanan, coba lagi"
            else -> {
                if (message.contains("AppwriteException", ignoreCase = true) ||
                    message.matches(Regex(".*\\d{4}\\s+.*")) ||
                    message.length < 15
                ) fallback
                else message
            }
        }
    }
}