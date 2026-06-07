// File: data/repository/AnimalRepository.kt
package com.yarsi.rescuepet.data.repository

import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.utils.Constants
import com.yarsi.rescuepet.utils.Result

class AnimalRepository {
    private val db = AppwriteClient.getDatabases()

    suspend fun postAnimal(animal: Animal): Result<String> {
        return try {
            val doc = db.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = "unique()",
                data = mapOf(
                    "type" to animal.type,
                    "name" to animal.name,
                    "age" to animal.age,
                    "description" to animal.description,
                    "imageId" to animal.imageId,
                    "status" to animal.status,
                    "latitude" to animal.latitude,
                    "longitude" to animal.longitude,
                    "posterId" to animal.posterId,
                    "posterContact" to animal.posterContact,
                    "category" to animal.category
                )
            )
            Result.Success(doc.id)
        } catch (e: AppwriteException) {
            Result.Error(e.message ?: "Gagal posting")
        }
    }

    suspend fun getAnimals(category: String? = null): Result<List<Animal>> {
        return try {
            val queries = mutableListOf<String>()
            if (category != null) queries.add(Query.equal("category", category))
            queries.add(Query.orderDesc("\$createdAt"))
            queries.add(Query.limit(50))

            val docs = db.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                queries = queries
            )
            Result.Success(docs.documents.map { it.toAnimal() })
        } catch (e: AppwriteException) {
            Result.Error(e.message ?: "Gagal load data")
        }
    }

    suspend fun getAnimalById(id: String): Result<Animal> {
        return try {
            val doc = db.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id
            )
            Result.Success(doc.toAnimal())
        } catch (e: AppwriteException) {
            Result.Error(e.message ?: "Gagal load data")
        }
    }

    suspend fun updateStatus(id: String, status: String): Result<Unit> {
        return try {
            db.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id,
                data = mapOf("status" to status)
            )
            Result.Success(Unit)
        } catch (e: AppwriteException) {
            Result.Error(e.message ?: "Gagal update")
        }
    }

    private fun Document<Map<String, Any>>.toAnimal() = Animal(
        id = id,
        type = data["type"] as? String ?: "",
        name = data["name"] as? String ?: "",
        age = (data["age"] as? Number)?.toInt() ?: 0,
        description = data["description"] as? String ?: "",
        imageId = data["imageId"] as? String ?: "",
        status = data["status"] as? String ?: "available",
        latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
        longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
        posterId = data["posterId"] as? String ?: "",
        posterContact = data["posterContact"] as? String ?: "",
        category = data["category"] as? String ?: "adoption"
    )
}
