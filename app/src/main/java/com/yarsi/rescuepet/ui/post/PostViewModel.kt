package com.yarsi.rescuepet.ui.post

import androidx.lifecycle.LiveData
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

    private val _postState = MutableLiveData<Result<String>>()
    val postState: LiveData<Result<String>> = _postState

    fun postAnimal(animal: Animal, imageFile: File?, imagePickFailed: Boolean = false) {
        if (imagePickFailed) {
            _postState.value = Result.Error("Gagal memproses foto, coba pilih ulang")
            return
        }
        _postState.value = Result.Loading
        viewModelScope.launch {
            val userResult = authRepo.getCurrentUser()
            if (userResult is Result.Error) {
                _postState.value = Result.Error("Silakan login terlebih dahulu")
                return@launch
            }
            val userId = (userResult as Result.Success).data

            var imageId = animal.imageId
            if (imageFile != null) {
                val uploadResult = storageRepo.uploadImage(imageFile)
                if (uploadResult is Result.Error) {
                    _postState.value = uploadResult
                    return@launch
                }
                imageId = (uploadResult as Result.Success).data

                imageFile.delete()
            }

            val animalWithMeta = animal.copy(imageId = imageId, posterId = userId)
            _postState.value = animalRepo.postAnimal(animalWithMeta)
        }
    }
}
