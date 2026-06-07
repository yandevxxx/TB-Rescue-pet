package com.yarsi.rescuepet.ui.post

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.data.repository.AuthRepository
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.utils.Result
import java.io.File

class PostViewModel : ViewModel() {
    private val animalRepo = AnimalRepository()
    private val storageRepo = StorageRepository()
    private val authRepo = AuthRepository()

    val postState = MutableLiveData<Result<String>>()

    fun postAnimal(animal: Animal, imageFile: File?, imagePickFailed: Boolean = false) {
        if (imagePickFailed) {
            postState.value = Result.Error("Gagal memproses foto, coba pilih ulang")
            return
        }
        postState.value = Result.Loading
        viewModelScope.launch {
            val userResult = authRepo.getCurrentUser()
            if (userResult is Result.Error) {
                postState.value = Result.Error("Silakan login terlebih dahulu")
                return@launch
            }
            val userId = (userResult as Result.Success).data

            var imageId = animal.imageId
            if (imageFile != null) {
                val uploadResult = storageRepo.uploadImage(imageFile)
                if (uploadResult is Result.Error) {
                    postState.value = uploadResult
                    return@launch
                }
                imageId = (uploadResult as Result.Success).data

                imageFile.delete()
            }

            val animalWithMeta = animal.copy(imageId = imageId, posterId = userId)
            postState.value = animalRepo.postAnimal(animalWithMeta)
        }
    }
}
