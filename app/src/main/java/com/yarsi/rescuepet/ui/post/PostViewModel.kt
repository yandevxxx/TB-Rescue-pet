package com.yarsi.rescuepet.ui.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.data.repository.AuthRepository
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.utils.Result
import com.yarsi.rescuepet.utils.validateContact
import com.yarsi.rescuepet.utils.validateLatitude
import com.yarsi.rescuepet.utils.validateLongitude
import kotlinx.coroutines.launch
import java.io.File

data class PostFormState(
    val type: String = "",
    val category: String = "",
    val name: String = "",
    val age: String = "",
    val description: String = "",
    val contact: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val contactError: String? = null,
    val latitudeError: String? = null,
    val longitudeError: String? = null,
    val isGettingLocation: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitResult: Result<String>? = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() && type.isNotBlank()
                && category.isNotBlank() && age.isNotBlank() && contact.isNotBlank()
}

class PostViewModel : ViewModel() {
    private val animalRepo = AnimalRepository()
    private val storageRepo = StorageRepository()
    private val authRepo = AuthRepository()

    private val _formState = MutableLiveData(PostFormState())
    val formState: LiveData<PostFormState> = _formState

    fun onTypeChanged(type: String) {
        _formState.value = _formState.value!!.copy(type = type)
    }

    fun onCategoryChanged(category: String) {
        _formState.value = _formState.value!!.copy(category = category)
    }

    fun onNameChanged(name: String) {
        _formState.value = _formState.value!!.copy(name = name)
    }

    fun onAgeChanged(age: String) {
        _formState.value = _formState.value!!.copy(age = age.filter { it.isDigit() })
    }

    fun onDescriptionChanged(description: String) {
        if (description.length <= 500) {
            _formState.value = _formState.value!!.copy(description = description)
        }
    }

    fun onContactChanged(contact: String) {
        _formState.value = _formState.value!!.copy(contact = contact, contactError = null)
    }

    fun onLatitudeChanged(lat: String) {
        _formState.value = _formState.value!!.copy(latitude = lat, latitudeError = null)
    }

    fun onLongitudeChanged(lon: String) {
        _formState.value = _formState.value!!.copy(longitude = lon, longitudeError = null)
    }

    fun onLocationResult(lat: Double, lon: Double) {
        _formState.value = _formState.value!!.copy(
            latitude = "%.6f".format(java.util.Locale.US, lat),
            longitude = "%.6f".format(java.util.Locale.US, lon),
            isGettingLocation = false
        )
    }

    fun setGettingLocation(value: Boolean) {
        _formState.value = _formState.value!!.copy(isGettingLocation = value)
    }

    fun resetSubmitResult() {
        _formState.value = _formState.value!!.copy(submitResult = null)
    }

    fun submit(imageFile: File?) {
        val state = _formState.value!!

        val contactError = validateContact(state.contact)
        val latitudeError = validateLatitude(state.latitude)
        val longitudeError = validateLongitude(state.longitude)

        if (contactError != null || latitudeError != null || longitudeError != null) {
            _formState.value = state.copy(
                contactError = contactError,
                latitudeError = latitudeError,
                longitudeError = longitudeError
            )
            return
        }

        if (imageFile != null && imageFile.length() > 5 * 1024 * 1024) {
            imageFile.delete()
            _formState.value = state.copy(submitResult = Result.Error("Ukuran file maksimal 5 MB"))
            return
        }

        _formState.value = state.copy(isSubmitting = true, submitResult = Result.Loading)

        viewModelScope.launch {
            try {
                val userResult = authRepo.getCurrentUser()
                if (userResult !is Result.Success) {
                    _formState.value = _formState.value!!.copy(
                        isSubmitting = false,
                        submitResult = Result.Error("Silakan login terlebih dahulu")
                    )
                    return@launch
                }
                val userData = userResult.data

                var imageId = ""
                if (imageFile != null) {
                    val uploadResult = storageRepo.uploadImage(imageFile)
                    if (uploadResult is Result.Error) {
                        _formState.value = _formState.value!!.copy(
                            isSubmitting = false,
                            submitResult = uploadResult
                        )
                        return@launch
                    }
                    imageId = (uploadResult as Result.Success).data
                }

                val animal = Animal(
                    type = state.type.lowercase(),
                    name = state.name,
                    age = state.age.toIntOrNull() ?: 0,
                    description = state.description,
                    status = "available",
                    latitude = state.latitude.toDoubleOrNull() ?: 0.0,
                    longitude = state.longitude.toDoubleOrNull() ?: 0.0,
                    posterContact = state.contact,
                    category = when (state.category) {
                        "Hilang" -> "lost"
                        "Ditemukan" -> "found"
                        else -> "adoption"
                    },
                    imageId = imageId,
                    posterId = userData.id,
                    posterName = userData.name
                )

                val result = animalRepo.postAnimal(animal)
                _formState.value = _formState.value!!.copy(
                    isSubmitting = false,
                    submitResult = result
                )
            } finally {
                imageFile?.delete()
            }
        }
    }
}
