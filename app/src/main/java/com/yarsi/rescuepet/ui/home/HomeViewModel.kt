package com.yarsi.rescuepet.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.appwrite.models.RealtimeSubscription
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.data.repository.AuthRepository
import com.yarsi.rescuepet.utils.Constants
import com.yarsi.rescuepet.utils.Result

class HomeViewModel : ViewModel() {
    private val repository = AnimalRepository()
    private var realtimeSubscription: RealtimeSubscription? = null
    private var currentCategory: String? = null

    private val _animals = MutableLiveData<List<Animal>>()
    val animals: LiveData<List<Animal>> = _animals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedFilter = MutableLiveData<String?>(null)
    val selectedFilter: LiveData<String?> = _selectedFilter

    fun loadAnimals(category: String? = null) {
        currentCategory = category
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getAnimals(category)) {
                is Result.Success -> {
                    _animals.value = result.data
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

    fun subscribeRealtime() {
        unsubscribeRealtime()
        val realtime = AppwriteClient.getRealtime()
        val channel = "databases.${Constants.DATABASE_ID}.collections.${Constants.COLLECTION_ANIMALS}.documents"
        realtimeSubscription = realtime.subscribe(channel) {
            loadAnimals(currentCategory)
        }
    }

    fun unsubscribeRealtime() {
        realtimeSubscription?.close()
        realtimeSubscription = null
    }

    fun setFilter(category: String?) {
        _selectedFilter.value = category
        loadAnimals(category)
    }

    fun logout() {
        viewModelScope.launch {
            AuthRepository().logout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeRealtime()
    }
}
