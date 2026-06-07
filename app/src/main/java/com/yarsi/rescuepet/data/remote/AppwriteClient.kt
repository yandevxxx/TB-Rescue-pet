package com.yarsi.rescuepet.data.remote

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Realtime
import com.yarsi.rescuepet.utils.Constants

object AppwriteClient {
    private lateinit var client: Client

    fun initialize(context: Context) {
        client = Client(context)
            .setEndpoint(Constants.APPWRITE_ENDPOINT)
            .setProject(Constants.APPWRITE_PROJECT_ID)
            .setSelfSigned(false)
    }

    fun getAccount() = Account(client)
    fun getDatabases() = Databases(client)
    fun getStorage() = Storage(client)
    fun getRealtime() = Realtime(client)
}
