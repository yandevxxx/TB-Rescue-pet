package com.yarsi.rescuepet.data.repository

import io.appwrite.exceptions.AppwriteException
import com.yarsi.rescuepet.data.model.UserData
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.utils.ErrorMapper
import com.yarsi.rescuepet.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val account by lazy { AppwriteClient.getInstance().getAccount() }

    suspend fun login(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            try { account.deleteSession("current") } catch (_: Exception) {}
            val session = account.createEmailPasswordSession(email, password)
            Result.Success(session.userId)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Login gagal"))
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            try { account.deleteSession("current") } catch (_: Exception) {}
            val user = account.create("unique()", email, password, name)
            try {
                account.createEmailPasswordSession(email, password)
            } catch (_: Exception) {
                return@withContext Result.Error("Akun berhasil dibuat, silakan login")
            }
            Result.Success(user.id)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Registrasi gagal"))
        }
    }

    suspend fun getCurrentUser(): Result<UserData> = withContext(Dispatchers.IO) {
        return@withContext try {
            val user = account.get()
            Result.Success(UserData(user.id, user.name))
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Tidak ada sesi aktif"))
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try { account.deleteSession("current") } catch (_: Exception) {}
    }
}
