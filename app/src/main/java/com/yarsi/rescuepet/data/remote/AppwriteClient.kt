package com.yarsi.rescuepet.data.remote

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Realtime
import com.yarsi.rescuepet.utils.Constants

class AppwriteClient(context: Context) {
    private val client = Client(context.applicationContext)
        .setEndpoint(Constants.APPWRITE_ENDPOINT)
        .setProject(Constants.APPWRITE_PROJECT_ID)
        .setSelfSigned(false)
    private var realtime: Realtime? = null

    fun getAccount() = Account(client)
    fun getDatabases() = Databases(client)
    fun getStorage() = Storage(client)
    fun getRealtime(): Realtime {
        if (realtime == null) {
            realtime = Realtime(client)
        }
        return realtime!!
    }

    companion object {
        @Volatile
        private var instance: AppwriteClient? = null

        fun initialize(context: Context) {
            instance ?: synchronized(this) {
                instance ?: AppwriteClient(context).also { instance = it }
            }
        }

        fun getInstance(): AppwriteClient {
            return instance ?: throw IllegalStateException("AppwriteClient not initialized. Call initialize() first.")
        }
    }
}
