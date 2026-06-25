// File: data/repository/AnimalRepository.kt
package com.yarsi.rescuepet.data.repository

import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.models.Document
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.utils.Constants
import com.yarsi.rescuepet.utils.ErrorMapper
import com.yarsi.rescuepet.utils.Result

class AnimalRepository {
    private val db = AppwriteClient.getInstance().getDatabases()

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
                    "posterName" to animal.posterName,
                    "posterContact" to animal.posterContact,
                    "category" to animal.category
                ),
                permissions = listOf(
                    Permission.read(Role.any()),
                    Permission.update(Role.user(animal.posterId)),
                    Permission.delete(Role.user(animal.posterId))
                )
            )
            Result.Success(doc.id)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal posting"))
        }
    }

    suspend fun getAnimalsPaged(
        category: String? = null,
        limit: Int = 20,
        cursorAfter: String? = null
    ): Result<Pair<List<Animal>, String?>> {
        return try {
            val queries = mutableListOf<String>()
            if (category != null) queries.add(Query.equal("category", category))
            queries.add(Query.orderDesc("\$createdAt"))
            queries.add(Query.limit(limit))
            if (cursorAfter != null) queries.add(Query.cursorAfter(cursorAfter))

            val docs = db.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                queries = queries
            )
            val animals = docs.documents.map { it.toAnimal() }
            val lastId = if (docs.documents.size >= limit) docs.documents.last().id else null
            Result.Success(Pair(animals, lastId))
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal load data"))
        }
    }

    suspend fun searchNearby(
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 10.0,
        category: String? = null
    ): Result<List<Animal>> {
        return try {
            val queries = mutableListOf<String>()
            if (category != null) queries.add(Query.equal("category", category))

            val latDelta = radiusKm / 111.0
            val lonDelta = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(userLat)))
            queries.add(Query.greaterThan("latitude", userLat - latDelta))
            queries.add(Query.lessThan("latitude", userLat + latDelta))
            queries.add(Query.greaterThan("longitude", userLon - lonDelta))
            queries.add(Query.lessThan("longitude", userLon + lonDelta))

            queries.add(Query.orderDesc("\$createdAt"))
            queries.add(Query.limit(100))

            val docs = db.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                queries = queries
            )
            Result.Success(docs.documents.map { it.toAnimal() })
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal load data"))
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
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal load data"))
        }
    }

    suspend fun updateAnimal(id: String, animal: Animal, userId: String): Result<Unit> {
        return try {
            db.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id,
                data = mapOf(
                    "type" to animal.type,
                    "name" to animal.name,
                    "age" to animal.age,
                    "description" to animal.description,
                    "imageId" to animal.imageId,
                    "category" to animal.category,
                    "latitude" to animal.latitude,
                    "longitude" to animal.longitude,
                    "posterContact" to animal.posterContact
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal update"))
        }
    }

    suspend fun updateStatus(id: String, status: String, userId: String): Result<Unit> {
        return try {
            val animal = db.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id
            )
            val posterId = animal.data["posterId"] as? String ?: ""
            if (posterId != userId) {
                return Result.Error("Anda bukan pemilik postingan ini")
            }
            db.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id,
                data = mapOf("status" to status)
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal update"))
        }
    }

    suspend fun deleteAnimal(id: String, userId: String): Result<Unit> {
        return try {
            val animal = db.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id
            )
            val posterId = animal.data["posterId"] as? String ?: ""
            if (posterId != userId) {
                return Result.Error("Anda bukan pemilik postingan ini")
            }
            val imageId = animal.data["imageId"] as? String ?: ""
            if (imageId.isNotEmpty()) {
                try {
                    AppwriteClient.getInstance().getStorage().deleteFile(Constants.BUCKET_ID, imageId)
                } catch (_: Exception) {}
            }
            db.deleteDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_ANIMALS,
                documentId = id
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e, "Gagal hapus"))
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
        posterName = data["posterName"] as? String ?: "",
        posterContact = data["posterContact"] as? String ?: "",
        category = data["category"] as? String ?: "adoption"
    )
}
