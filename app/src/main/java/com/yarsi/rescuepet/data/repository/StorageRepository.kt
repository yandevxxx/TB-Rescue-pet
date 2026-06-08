package com.yarsi.rescuepet.data.repository

import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.utils.Constants
import com.yarsi.rescuepet.utils.ErrorMapper
import com.yarsi.rescuepet.utils.Result
import java.io.File

class StorageRepository {
    private val storage = AppwriteClient.getInstance().getStorage()

    suspend fun uploadImage(file: File): Result<String> {
        return try {
            val uploaded = storage.createFile(
                bucketId = Constants.BUCKET_ID,
                fileId = "unique()",
                file = InputFile.fromFile(file)
            )
            Result.Success(uploaded.id)
        } catch (e: AppwriteException) {
            Result.Error(ErrorMapper.map(e, "Gagal upload foto"))
        }
    }

    fun getImageUrl(imageId: String): String {
        return "${Constants.APPWRITE_ENDPOINT}/storage/buckets/" +
                "${Constants.BUCKET_ID}/files/$imageId/view?" +
                "project=${Constants.APPWRITE_PROJECT_ID}"
    }
}
