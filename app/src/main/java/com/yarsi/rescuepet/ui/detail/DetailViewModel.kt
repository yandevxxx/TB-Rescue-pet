package com.yarsi.rescuepet.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.data.repository.AuthRepository
import com.yarsi.rescuepet.utils.Result

class DetailViewModel : ViewModel() {
    private val animalRepo = AnimalRepository()
    private val authRepo = AuthRepository()

    val animal = MutableLiveData<Animal?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val updateState = MutableLiveData<Result<Unit>>()
    val currentUserId = MutableLiveData<String?>()
    private var currentAnimalId: String? = null

    fun loadAnimal(id: String) {
        currentAnimalId = id
        isLoading.value = true
        viewModelScope.launch {
            val userResult = authRepo.getCurrentUser()
            if (userResult is Result.Success) {
                currentUserId.value = userResult.data
            }
            when (val result = animalRepo.getAnimalById(id)) {
                is Result.Success -> {
                    animal.value = result.data
                    error.value = null
                }
                is Result.Error -> {
                    error.value = result.message
                }
                else -> {}
            }
            isLoading.value = false
        }
    }

    fun reloadAnimal() {
        currentAnimalId?.let { loadAnimal(it) }
    }

    val deleteState = MutableLiveData<Result<Unit>>()

    fun updateStatus(animalId: String, newStatus: String) {
        updateState.value = Result.Loading
        viewModelScope.launch {
            val result = animalRepo.updateStatus(animalId, newStatus)
            updateState.value = result
            if (result is Result.Success) {
                reloadAnimal()
            }
        }
    }

    fun deleteAnimal(animalId: String) {
        deleteState.value = Result.Loading
        viewModelScope.launch {
            deleteState.value = animalRepo.deleteAnimal(animalId)
        }
    }
}
