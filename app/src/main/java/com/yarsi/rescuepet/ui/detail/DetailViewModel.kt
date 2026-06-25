package com.yarsi.rescuepet.ui.detail

import androidx.lifecycle.LiveData
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

    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _updateState = MutableLiveData<Result<Unit>>()
    val updateState: LiveData<Result<Unit>> = _updateState

    private val _currentUserId = MutableLiveData<String?>()
    val currentUserId: LiveData<String?> = _currentUserId

    private val _currentRole = MutableLiveData<String?>()
    val currentRole: LiveData<String?> = _currentRole

    private val _deleteState = MutableLiveData<Result<Unit>>()
    val deleteState: LiveData<Result<Unit>> = _deleteState

    private var currentAnimalId: String? = null

    fun loadAnimal(id: String) {
        currentAnimalId = id
        _isLoading.value = true
        viewModelScope.launch {
            val userResult = authRepo.getCurrentUser()
            if (userResult is Result.Success) {
                _currentUserId.value = userResult.data.id
            }
            _currentRole.value = authRepo.getRole()
            when (val result = animalRepo.getAnimalById(id)) {
                is Result.Success -> {
                    _animal.value = result.data
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun reloadAnimal() {
        currentAnimalId?.let { loadAnimal(it) }
    }

    fun updateStatus(animalId: String, newStatus: String) {
        val uid = _currentUserId.value ?: run {
            _updateState.value = Result.Error("Silakan login terlebih dahulu")
            return
        }
        _updateState.value = Result.Loading
        viewModelScope.launch {
            val result = animalRepo.updateStatus(animalId, newStatus, uid)
            _updateState.value = result
            if (result is Result.Success) {
                reloadAnimal()
            }
        }
    }

    fun deleteAnimal(animalId: String) {
        val uid = _currentUserId.value ?: run {
            _deleteState.value = Result.Error("Silakan login terlebih dahulu")
            return
        }
        _deleteState.value = Result.Loading
        viewModelScope.launch {
            _deleteState.value = animalRepo.deleteAnimal(animalId, uid)
        }
    }
}
