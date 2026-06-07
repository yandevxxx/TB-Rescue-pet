package com.yarsi.rescuepet.data.repository

import io.appwrite.exceptions.AppwriteException
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.utils.Result

class AuthRepository {
    private val account = AppwriteClient.getAccount()

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            try { account.deleteSession("current") } catch (_: Exception) {}
            val session = account.createEmailPasswordSession(email, password)
            Result.Success(session.userId)
        } catch (e: AppwriteException) {
            Result.Error(e.message ?: "Login gagal")
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<String> {
        return try {
            try { account.deleteSession("current") } catch (_: Exception) {}
            val user = account.create("unique()", email, password, name)
            try {
                account.createEmailPasswordSession(email, password)
            } catch (_: AppwriteException) {
                return Result.Error("Akun berhasil dibuat, silakan login")
            }
            Result.Success(user.id)
        } catch (e: AppwriteException) {
            Result.Error(e.message ?: "Registrasi gagal")
        }
    }

    suspend fun getCurrentUser(): Result<String> {
        return try {
            val user = account.get()
            Result.Success(user.id)
        } catch (e: AppwriteException) {
            Result.Error("Tidak ada sesi aktif")
        }
    }

    suspend fun logout() {
        try { account.deleteSession("current") } catch (_: Exception) {}
    }
}
